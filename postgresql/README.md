## ITCS 3160-0002, Spring 2024
## Marco Vieira, marco.vieira@charlotte.edu
## University of North Carolina at Charlotte

The code and resources provided are to be used only in the scope of ITCS 3160-0002, Spring 2024.


## Requirements

- To execute this project it is required to have installed:
  - Docker

## Development

Use only if you need to have database running in separate.
The executables in the root are prepared to start the database and connect it with Web Application.

It could be useful to have it running in separate for the Java example, where it is not possible for you to change code being executed by Docker and access does changes without starting docker components again.

## Database Connection

- **User**: scott
- **Password**: tiger
- **Database name**: dbproj
- **Host**: localhost:5432

## Setup and Run

To build the docker image you should run:

```sh
sh build.sh
```

To run the container:

```sh
sh run.sh
```

- _note: modifying the `run.sh` script to include -dit will make the container work in background. But dont forget to use `stop.sh` to stop/remove it later._

To stop the container:

```sh
sh stop.sh
```

