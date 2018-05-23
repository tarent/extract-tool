#!/usr/bin/env mksh
# -*- mode: sh -*-
#-
# Copyright © 2016, 2017
#	mirabilos <t.glaser@tarent.de>
#
# Provided that these terms and disclaimer and all copyright notices
# are retained or reproduced in an accompanying document, permission
# is granted to deal in this work without restriction, including un‐
# limited rights to use, publicly perform, distribute, sell, modify,
# merge, give away, or sublicence.
#
# This work is provided “AS IS” and WITHOUT WARRANTY of any kind, to
# the utmost extent permitted by applicable law, neither express nor
# implied; without malicious intent or gross negligence. In no event
# may a licensor, author or contributor be held liable for indirect,
# direct, other damage, loss, or other issues arising in any way out
# of dealing in the work, even if advised of the possibility of such
# damage or existence of a defect, except proven that it results out
# of said person’s immediate fault when using the work as intended.
#-
# Script to check dependencies

# initialisation
LC_ALL=C; export LC_ALL
unset LANGUAGE
PS4='++ '
set -e
set -o pipefail
cd "$(dirname "$0")"

# check old file is sorted
sort -uo ckdep.tmp ckdep.lst
if cmp -s ckdep.lst ckdep.tmp; then
	abend=0
else
	print -ru2 -- '[WARNING] list of dependencies was not sorted!'
	cat ckdep.tmp >ckdep.lst
	abend=1
fi
# analyse Maven dependencies
(cd ../.. && mvn -B -P '!test-only-dependencies' dependency:list) 2>&1 | \
    tee /dev/stderr | sed -n \
    -e '/:test$/d' \
    -e '/^\[INFO]    org.evolvis.tartools:extract-tool/d' \
    -e '/^\[INFO]    \([^:]*\):\([^:]*\):jar:\([^:]*\):[^:]*$/s//\1:\2 \3 ok/p' \
    >ckdep.tmp
# add static dependencies from embedded files, for SecurityWatch
[[ -s ckdep.inc ]] && cat ckdep.inc >>ckdep.tmp
# generate file with changed dependencies set to be a to-do item
sort -uo ckdep.tmp ckdep.tmp
{
	comm -13 ckdep.lst ckdep.tmp | sed 's/ ok$/ TO''DO/'
	comm -12 ckdep.lst ckdep.tmp
} | sort -o ckdep.tmp

# check if the list changed
if cmp -s ckdep.lst ckdep.tmp; then
	print -ru2 -- '[INFO] list of dependencies did not change'
else
	(diff -u ckdep.lst ckdep.tmp || :)
	# make the new list active
	mv -f ckdep.tmp ckdep.lst
	# inform the user
	print -ru2 -- '[WARNING] list of dependencies changed!'
	abend=1
fi
# check if anything needs to be committed
if (( abend )); then
	print -ru2 -- '[ERROR] please commit the changed ckdep.lst file!'
	exit 1
fi

# fail a release build if dependency licence review has a to-do item
[[ $IS_M2RELEASEBUILD = true ]] && \
    if grep -e ' TO''DO$' -e ' FA''IL$' ckdep.lst; then
	print -ru2 -- '[ERROR] licence review incomplete'
	exit 1
fi

rm -f ckdep.tmp
exit 0
