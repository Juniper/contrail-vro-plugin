#!/usr/bin/env bash

#
# Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
#

set -e

if [ "$#" -ne 2 ]; then
    echo "Repository URL and directory name needs to be specified."
    exit 1
fi

URL=$1
DIR=$2

if [ -L ${DIR} ]; then
    echo "Will not update repository that is a symbolic link."
    exit 1
fi

git clone ${URL} ${DIR} || echo "Repository ${DIR} already exists."
cd ${DIR}
git checkout -B master origin/master
git pull

cd ..
