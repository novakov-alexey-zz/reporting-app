extern crate dotenv;
extern crate log;
extern crate serde;
extern crate uuid;

use self::serde::ser::Serialize;
use self::uuid::Uuid;
use super::fs_utils::*;
use super::templates::TemplateEngine;
use service::dotenv::dotenv;
use std::env;
use std::fs;
use std::io::Write;
use std::process::Command;
use std::process::Output;
use std::process::Stdio;

const USE_STDIN_MARKER: &str = "-";
const WKHTMLTOPDF_CMD: &str = "wkhtmltopdf";
const NO_WKHTMLTOPDF_ERR: &str = "wkhtmltopdf tool is not found. Please install it.";

type PdfPath = String;

pub struct ReportService {
    template_engine: TemplateEngine,
    work_dir: String,
}

#[derive(Debug)]
pub struct ServiceError(String);

#[derive(Debug)]
pub struct RenderingError(String);

impl ReportService {
    pub fn new() -> Result<Self, ServiceError> {
        ReportService::bootstrap_checks().map_err(|e| ServiceError(e))?;
        dotenv().ok();

        let work_dir = ReportService::init_work_dir();
        let template_engine = TemplateEngine::new().map_err(|e| {
            ServiceError(format!("Failed to create template engine, error: {:?}", e))
        })?;

        Ok(ReportService {
            template_engine,
            work_dir,
        })
    }

    fn init_work_dir() -> String {
        let work_dir = env::var("WORK_DIR").unwrap_or_else(|_| "target/work_dir".to_string());
        let dir_entries = fs::read_dir(&work_dir);
        delete_dir_contents(dir_entries);

        fs::create_dir_all(&work_dir).err().iter().for_each(|e| {
            warn!(
                "Warning: could not create working directory '{}', error: {}",
                &work_dir, e
            )
        });
        work_dir
    }

    fn bootstrap_checks() -> Result<(), String> {
        info!("Bootstrap check for {} tool", WKHTMLTOPDF_CMD);
        let status = Command::new(WKHTMLTOPDF_CMD)
            .arg("-V")
            .spawn()
            .map_err(|e| format!("Failed to spawn child process: {}", e))
            .and_then(|mut p| {
                p.wait().map_err(|e| {
                    format!("Failed to wait for {} tool , error: {}", WKHTMLTOPDF_CMD, e)
                })
            });

        status
            .and_then(|s| {
                if s.success() {
                    Ok(())
                } else {
                    Err(NO_WKHTMLTOPDF_ERR.to_string())
                }
            })
            .map_err(|e| {
                error!("{:?}", e);
                NO_WKHTMLTOPDF_ERR.to_string()
            })
    }

    pub fn render<T>(&self, template_name: String, data: T) -> Result<PdfPath, RenderingError>
    where
        T: Serialize + std::fmt::Debug,
    {
        debug!("rendering report for data {:?}", &data);
        let html = self
            .template_engine
            .render(&template_name, &data)
            .map_err(|e| RenderingError(format!("Failed to render, error: {:?}", e)))?;

        let destination_pdf = self.dest_name(&template_name);

        debug!("destination PDF {}", &destination_pdf);
        let output = ReportService::run_blocking(html, &destination_pdf)?;

        debug!("status: {}", output.status);
        debug!("stdout: {}", String::from_utf8_lossy(&output.stdout));
        debug!("stderr: {}", String::from_utf8_lossy(&output.stderr));

        schedule_file_deletion(destination_pdf.clone());
        Ok(destination_pdf)
    }

    fn dest_name(&self, template_name: &str) -> PdfPath {
        format!("{}/{}-{}.pdf", self.work_dir, Uuid::new_v4(), template_name)
    }

    fn run_blocking(html: String, destination_pdf: &str) -> Result<Output, RenderingError> {
        let mut child = Command::new(WKHTMLTOPDF_CMD)
            .stdin(Stdio::piped())
            .stdout(Stdio::null())
            .arg(USE_STDIN_MARKER)
            .arg(&destination_pdf)
            .spawn()
            .map_err(|e| RenderingError(format!("Failed to spawn child process: {}", e)))?;
        {
            let stdin = child
                .stdin
                .as_mut()
                .ok_or(RenderingError("Failed to open stdin".to_string()))?;

            stdin
                .write_all(html.as_bytes())
                .map_err(|e| RenderingError(format!("Failed to write stdin, error: {}", e)))?;
        }

        let output = child
            .wait_with_output()
            .map_err(|e| RenderingError(format!("Failed to read stdout, error: {}", e)))?;

        Ok(output)
    }
}
