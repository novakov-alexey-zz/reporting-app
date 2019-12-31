extern crate log;
extern crate rocket;

use rocket::http::Method;
use rocket::response::NamedFile;
use rocket_contrib::json::Json;
use rocket_contrib::json::JsonValue;

use cors::PreflightCORS;
use cors::CORS;
use service::ReportService;

use self::rocket::State;

#[derive(Deserialize)]
pub struct GenRequest {
    template_name: String,
    user_params: JsonValue,
}

#[get("/")]
pub fn index() -> &'static str {
    "Hello, api-report!"
}

#[options("/generate")]
pub fn generate_pre_flight_req() -> PreflightCORS {
    CORS::preflight("*")
        .methods(vec![Method::Options, Method::Post])
        .headers(vec!["Content-Type"])
}

#[post("/generate", format = "application/json", data = "<req>")]
pub fn generate_report(
    service: State<ReportService>,
    req: Json<GenRequest>,
) -> CORS<Result<NamedFile, Json<String>>> {
    let params = req.0.user_params;
    let report = service.render(req.0.template_name, params);

    CORS::any(
        report
            .map_err(|e| Json(format!("Failed to generate report: {:?}", e)))
            .and_then(|path| NamedFile::open(path).map_err(|e| Json(e.to_string()))),
    )
}
