# Polyclinic Registry

**Java 17 · JavaFX · Maven · H2 · JDBC · TCP sockets · Multithreading**

An educational client–server desktop application for managing a **simulated** clinic registry. A JavaFX desktop client communicates with a multithreaded Java server using serialized request/response objects; the server stores data in an embedded H2 database via JDBC.

> **Academic demonstration, not medical software.** This project has no production-grade authentication, transport security, access control, or privacy safeguards. Use synthetic data only. The demo server is bound to **localhost** by default to avoid exposing the protocol over a network.

## Features

- Patient records: create, view, search, edit, and delete
- Doctor directory and appointment scheduling
- Appointment conflict checking for a doctor's date/time slot
- Client–server request/response protocol and individual client handler threads
- Automatically initialized local H2 database with **fictional** example records
- Desktop UI built with JavaFX and FXML

## Architecture

```text
PoliclinicRegistry/
├── common/              Shared data models and request/response types
├── server/              Java TCP server, JDBC repositories and H2 database
├── client/              JavaFX client, FXML controller and screens
├── pom.xml              Multi-module Maven parent
├── README.md            English documentation
└── README_RUS.md        Original Russian documentation
```

### Data model

The embedded database has three tables: `Patients`, `Doctors`, and `Appointments`. Appointments reference a patient and doctor, with a unique constraint on doctor/date/time. Model classes and network types are shared by the client and server through the `common` module.

## Run locally

Requires **JDK 17** and **Maven**. From the repository root:

```bash
mvn clean install
java -jar server/target/server-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Start the JavaFX client in a **second terminal**:

```bash
mvn -f client/pom.xml javafx:run
```

The server runs locally on port **12345** and uses an embedded H2 database. No separate database installation is needed. The client requires a graphical desktop.

## Design details

- The `common` module defines serializable `Request` and `Response` objects and domain models.
- The server accepts TCP connections on loopback and handles each client in its own thread.
- JDBC `PreparedStatement` is used for database updates and searches.
- H2 tables are initialized on the first run and seeded with fictional demonstration records.
- Appointment uniqueness is enforced by a database constraint.

## Known limitations

- **Do not use with real patient information.** Authentication in the demo is not suitable for protecting medical data; object serialization is not hardened against untrusted clients.
- No TLS or network-facing deployment is supported.
- Some UI features and full client–server workflows are **not yet covered by automated integration tests**.
- The database and compiled output belong on a developer's machine; neither should be committed.
- Future improvements: automated integration tests, proper authentication/authorization, a safer API protocol, improved error handling, and a reproducible GUI test environment.

## Project context

Built as a university project in a programming technologies course. The original Russian-language description is preserved in [README_RUS.md](README_RUS.md).

**Author:** Marina Erokhina
