########################################
# A script that makes MirthConnect tarball as a Maven artifact in the local repository
#
# Kiran Ayyagari (kayyagari@apache.org)
########################################

help() {
	echo "Usage:"
	echo "./upload-mc-to-maven.sh <mc-tar-file> <mc-version>"
	exit 1
}

if [ "$#" -ne 2 ]; then
	echo "invalid arguments"
	help
fi

MC_TARBALL=$1
MC_VERSION=$2
echo  "making a Maven artifact from MC archive " $MC_TARBALL

mvn install:install-file -DgroupId="com.mirth.connect" -DartifactId="mirthconnect-archive" -Dversion=$MC_VERSION -Dpackaging=tar.gz -Dfile="$MC_TARBALL"
