/**
 *
 * This Groovy script is executed using GMaven. See http://docs.codehaus.org/display/GMAVEN/Executing+Groovy+Code
 */

static final String VERSION_STRING_PROPERTY = 'idea.version.string'

/**
 * Returns the configuration directory for a given version of IntelliJ IDEA.
 *
 * @param versionString a String specifying the IDEA version (e.g. IntelliJIDEA90, IntelliJIDEA10).
 * @return
 */
File getConfigDir(String versionString) {
  String userHome = session.executionProperties['user.home']
  String operatingSys = session.executionProperties['os.name']

  if (operatingSys.contains("windows")) {
    // e.g. C:\Users\<user_dir>\.IntelliJIdea60\config	user_dir is your account
    return new File("$userHome\\\\$versionString\\\\config")
  }
  else if (operatingSys.contains("Mac")) {
    // e.g. ~/Library/Preferences/IntelliJIDEA60
    return new File("$userHome/Library/Preferences/$versionString")
  }
  else {
    // e.g. ~/.IntelliJIdea60/config
    return new File("$userHome/.$versionString/config")
  }
}

/**
 * Copies all the files in the given sub-directory of the ideaConfig directory into the IDEA configuration.
 *
 * @param subdir a String containing the IDEA subdirectory
 * @param todir the IDEA config dir
 */
void copySubdirTo(String subdir, String todir) {
  String baseDir = project['basedir']
  String fileSetDir = "$baseDir/src/main/resources/ideaConfig/$subdir"
  String targetDir = "$todir/$subdir"

  log.info "\tCopying $fileSetDir to $targetDir"

  ant.copy(todir: targetDir, overwrite: true) {
    fileset(dir: fileSetDir)
  }
}

void copyIdeaConfiguration(File configDir) {
  log.info "\t Updating IDEA templates in $configDir"

  copySubdirTo("templates", "$configDir.absolutePath")
  copySubdirTo("fileTemplates", "$configDir.absolutePath")

}

boolean tryToUpdateIDEAConfig(String versionString) {
	File configDir = getConfigDir(versionString)
	log.info "\tLooking to update IDEA templates in $configDir"
	if (configDir.exists()) {
		copyIdeaConfiguration(configDir);
		log.info "\tUpdated IDEA templates in $configDir"
		return true;
	}
	return false;
}

log.info "IDEA update config groovy script running.."

String versionString = 'IntelliJIdea10'
if (project.properties.containsKey(VERSION_STRING_PROPERTY)) {
  versionString = project.properties[VERSION_STRING_PROPERTY]
}

if (! tryToUpdateIDEAConfig(versionString)) {
	if (! tryToUpdateIDEAConfig('IntelliJIdea09')) {
		log.error("\tUnable to find your IDEA config direcrtory");
	}
}

log.info "IDEA update config groovy script finished"

