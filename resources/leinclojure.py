#!/usr/bin/env python
import os
import sys


def main(lein_working_directory, argv):
    os.chdir(lein_working_directory)
    os.execvp('clj', ['clj', '-A:jupyter', '-m', 'provisdom.deps-jupyter', 'kernel'] + argv)

if __name__ == '__main__':
    if 'PROJECT_WORKING_DIRECTORY' not in os.environ:
        raise RuntimeError("The environment variable 'PROJECT_WORKING_DIRECTORY'"
                           " for this kernel to run")
    main(os.environ['PROJECT_WORKING_DIRECTORY'], sys.argv[1:])
