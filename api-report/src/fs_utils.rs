extern crate log;

use std::fs;
use std::fs::ReadDir;
use std::io::Error;
use std::thread;
use std::time::Duration;

pub fn schedule_file_deletion(file_name: String) {
    thread::spawn(move || {
        debug!("Going to remove a file: {}", file_name);
        thread::sleep(Duration::from_secs(3));
        match fs::remove_file(&file_name) {
            Ok(_) => debug!("Removed file: {}", file_name),
            Err(e) => error!("Failed to schedule file removal: {}", e),
        }
    });
}

pub fn delete_dir_contents(read_dir_res: Result<ReadDir, Error>) {
    if let Ok(dir) = read_dir_res {
        for entry in dir {
            if let Ok(entry) = entry {
                let path = entry.path();

                if path.is_dir() {
                    fs::remove_dir_all(path).expect("Failed to remove a dir");
                } else {
                    fs::remove_file(path).expect("Failed to remove a file");
                }
            };
        }
    };
}
