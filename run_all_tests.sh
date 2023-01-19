#!/usr/bin/env bash

sbt clean scalafmtAll coverage test coverageReport
