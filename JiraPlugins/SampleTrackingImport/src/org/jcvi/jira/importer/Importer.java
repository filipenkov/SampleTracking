package org.jcvi.jira.importer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.xmlbeans.XmlException;
import org.hibernate.Session;
import org.jcvi.common.command.CommandLineOptionBuilder;
import org.jcvi.common.command.CommandLineUtils;
import org.jcvi.commonx.auth.tigr.ProjectDbAuthorizer;
import org.jcvi.commonx.auth.tigr.ProjectDbAuthorizerUtils;
import org.jcvi.glk.ctm.CTMElviraGLKSessionBuilder;
import org.jcvi.glk.ctm.CTMHelper;
import org.jcvi.glk.ctm.HibernateCTMHelper;
import org.jcvi.glk.helpers.GLKHelper;
import org.jcvi.glk.helpers.HibernateGLKHelper;
import org.jcvi.jira.importer.mappers.UnmappedField;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 */
public class Importer {
    private static final long version = 201110101233l;

    public static void main(String[] args) {
        System.out.println("Started");
        System.out.println("Version: " + version);

        Options options = new Options();
        ProjectDbAuthorizerUtils.addMultipleProjectDbLoginOptionsTo(options, true);
        options.addOption(CommandLineUtils.createHelpOption());
        options.addOption(
                new CommandLineOptionBuilder("a","Include all, not just active, samples")
                        .longName("all")
                        .isRequired(false)
                        .isFlag(true)
                        .build());
//        options.addOption(new CommandLineOptionBuilder("o", "path to output  .xls file")
//                .isRequired(true)
//                .build());

        if(CommandLineUtils.helpRequested(args)){
            printHelp(options);
            System.exit(0);
        }
        try {
            CommandLine commandLine = CommandLineUtils.parseCommandLine(options, args);
//            if (commandLine.hasOption('f')) {
                //commandLine.getOptionValue('f');
            boolean activeOnly = !commandLine.hasOption("all");

            //try creating the output file early incase it doesn't work
            //todo: use a parameter
            File outputFile  = new File("/home/pedworth/test.xml");

            ClassLoader.getSystemResourceAsStream("StatusMappings.csv");
            //Reader xmlLookups  = new FileReader("/home/pedworth/workspace/SampleTrackingImport/src/Lookups.xml");
            //Reader statusesCSV = new FileReader("/home/pedworth/workspace/SampleTrackingImport/src/StatusMappings.csv");
            Reader xmlLookups = new InputStreamReader(
                    ClassLoader.getSystemResourceAsStream("resources/Lookups.xml"));
            Reader statusesCSV = new InputStreamReader(
                    ClassLoader.getSystemResourceAsStream("resources/StatusMappings.csv"));
            XMLImporter importer = new XMLImporter(xmlLookups,statusesCSV);

            List<ProjectDbAuthorizer> databases =
                    ProjectDbAuthorizerUtils.getMultipleProjectDbAuthorizersFrom(commandLine, System.console());
            Set<UnmappedField> failedMappings = new HashSet<UnmappedField>();
            for (ProjectDbAuthorizer dbAuth: databases) {
                Session session = new CTMElviraGLKSessionBuilder(dbAuth).build();
                CTMHelper ctmHelper = new HibernateCTMHelper(session);
                GLKHelper glkHelper = new HibernateGLKHelper(session);
                importer.processCTMData(ctmHelper,glkHelper,dbAuth.getProject(), activeOnly, true /*verbose errors*/);
                failedMappings.addAll(importer.getFailedMappings());

            }
            //output now in case calling outputResults throws an Exception
            System.err.println(XMLImporter.convertToErrorString("Failed to map: ",failedMappings));

            importer.outputResults(outputFile);
        } catch (XmlException xml) {
            System.err.println("problem with creating the XMLImporter");
            System.err.println(xml.getMessage());
            System.exit(5);
        } catch (FileNotFoundException fnfe) {
            //thrown only if the 'use passwords file' option is used
            //ProjectDbAuthorizerUtils.getMultipleProjectDbAuthorizersFrom(commandLine, System.console());
            //should throw this
            printHelp(options);
            System.err.println(fnfe.getMessage());
            System.exit(4);
        } catch (IOException io) {
            System.err.println("problem with creating the XMLImporter, or writing the output");
            System.err.println(io.getMessage());
            System.exit(3);

//        } catch (IllegalArgumentException iae) {
//            //parsing the cmdline failed
//            printHelp(options);
//            System.err.println(iae.getMessage());
//            System.exit(2);
        } catch (ParseException e) {
            printHelp(options);
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "cmdname [--option]",
                "description",
                options,
                "Created by JCVI");
    }
}
