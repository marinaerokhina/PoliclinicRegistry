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

## Requirements

- **JDK 17**
- **Maven 3.8+**
- A graphical desktop environment for the JavaFX client

No external database server is required: H2 runs inside the server process.

## Build and run

All commands assume that you have cloned the repository:

```bash
git clone https://github.com/marinaerokhina/PoliclinicRegistry.git
cd PoliclinicRegistry
mvn clean install
```

### 1. Start the server

From the **repository root**, run:

```bash
java -jar server/target/server-1.0-SNAPSHOT-jar-with-dependencies.jar
```

The default TCP port is **12345**, restricted to the local machine (`127.0.0.1`). You can optionally pass a different port as the first argument:

```bash
java -jar server/target/server-1.0-SNAPSHOT-jar-with-dependencies.jar 12346
```

The embedded database file `polyclinic_registry_db.mv.db` is created in the working directory. It is intentionally ignored by Git.

### 2. Start the JavaFX client

Open **another terminal** from the repository root:

```bash
mvn -f client/pom.xml javafx:run
```

The client defaults to `127.0.0.1:12345`. To match a non-default server port, set the client Java property `polyclinic.port` (for example, via the JavaFX plugin JVM options or an IDE run configuration).

**Note:** A headless CI build can compile the project and smoke-test the server, but cannot validate the desktop GUI. A local graphical session is needed to verify JavaFX behavior and end-to-end interaction.

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
