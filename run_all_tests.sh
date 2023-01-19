#!/usr/bin/env bash

sbt clean scalastyle scalafmtAll scalafixAll coverage test coverageReport
