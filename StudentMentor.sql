CREATE TABLE students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE mentors (
    mentor_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    profile_name VARCHAR(100),  -- Mentor’s real name shown in dropdown
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE student_projects (
    project_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,         -- Linked to student who created it
    team_name VARCHAR(100) NOT NULL,
    member1_name VARCHAR(100),
    member1_regno VARCHAR(20),
    member2_name VARCHAR(100),
    member2_regno VARCHAR(20),
    member3_name VARCHAR(100),
    member3_regno VARCHAR(20),
    project_title VARCHAR(200) NOT NULL,
    project_description TEXT,
    chosen_mentor_id INT,            -- Mentor selected from dropdown
    status ENUM('Pending','Accepted','Rejected') DEFAULT 'Pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (chosen_mentor_id) REFERENCES mentors(mentor_id)
);


CREATE TABLE mentor_team_map (
    id INT AUTO_INCREMENT PRIMARY KEY,
    mentor_id INT NOT NULL,
    project_id INT NOT NULL,
    accepted_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (mentor_id) REFERENCES mentors(mentor_id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES student_projects(project_id) ON DELETE CASCADE
);