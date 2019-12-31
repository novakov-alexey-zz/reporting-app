#![feature(plugin)]
#![feature(proc_macro_hygiene, decl_macro)]
extern crate handlebars;
#[macro_use]
extern crate log;
#[macro_use]
extern crate rocket;
extern crate rocket_contrib;
#[macro_use]
extern crate serde_derive;

use rocket::Rocket;

use routes::*;
use service::ReportService;

mod cors;
mod fs_utils;
mod handlebars_ext;
pub mod routes;
pub mod service;
mod templates;

pub fn mount_routes(service: ReportService) -> Rocket {
    rocket::ignite().manage(service).mount(
        "/api/v1",
        routes![index, generate_report, generate_pre_flight_req],
    )
}
