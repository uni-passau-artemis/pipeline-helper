#!/usr/bin/env sh

awk -F"," '{
        instructions += $4 + $5;
        covered += $5;
        branches += $6 + $7;
        branchesCovered += $7;
    } END {
        print "Instructions Covered:", covered, "/", instructions;
        print "Instruction Coverage:", 100*covered/instructions, "%";
        print "Branches Covered:", branchesCovered, "/", branches;
        print "Branch Coverage: ", 100*branchesCovered/branches, "%";
    }' "$1"
