#!/bin/bash
set -e

bundle install
PORT="${PORT:-4567}"
ruby app.rb -p "$PORT"
