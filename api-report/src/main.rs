#![feature(plugin)]
extern crate api_report;
extern crate env_logger;
#[macro_use]
extern crate log;

use api_report::mount_routes;
use api_report::service::ReportService;
use env_logger::{Builder, Target};
use std::env;

fn main() {
    init_logger();

    info!("Starting api-report...");
    match ReportService::new() {
        Ok(s) => {
            let error = mount_routes(s).launch();
            drop(error);
        }
        Err(e) => {
            error!("Failed to start api-report service, error: {:?}", e);
            panic!(e)
        }
    }
}

fn init_logger() {
    let mut builder = Builder::new();
    builder.target(Target::Stdout);
    if env::var("RUST_LOG").is_ok() {
        builder.parse(&env::var("RUST_LOG").unwrap());
    }
    builder.init();
}
