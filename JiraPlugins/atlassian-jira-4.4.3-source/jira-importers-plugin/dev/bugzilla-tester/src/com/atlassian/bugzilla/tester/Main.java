package com.atlassian.bugzilla.tester;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.net.WebLocation;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaAttribute;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaAttributeMapper;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaClient;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaTaskDataHandler;
import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

public class Main {

	private static BugzillaClient[] clients = new BugzillaClient[4];

	public static void main(String[] args) throws Exception {
		final String url = "http://192.168.157.160/bugzilla3.6.2/";
		AbstractWebLocation webLocation = new WebLocation(url, "wseliga@atlassian.com",
				"d0n0tch@nge");

		TaskRepository repository = new TaskRepository(BugzillaCorePlugin.CONNECTOR_KIND, url);
		repository.setCredentials(AuthenticationType.REPOSITORY, webLocation.getCredentials(AuthenticationType.REPOSITORY),
				false);
		final BugzillaRepositoryConnector connector = new BugzillaRepositoryConnector();
		BugzillaClient client = new BugzillaClient(webLocation, repository, connector);
//		client.authenticate(new NullProgressMonitor());
		
		for (int i = 0; i < clients.length; i++) {
			AbstractWebLocation webLocation2 = new WebLocation(url, "user" + (i + 1) + "@example.com",
					"password");
			TaskRepository repository2 = new TaskRepository(BugzillaCorePlugin.CONNECTOR_KIND, url);
			repository2.setCredentials(AuthenticationType.REPOSITORY,
					webLocation.getCredentials(AuthenticationType.REPOSITORY),
					false);
			clients[i] = new BugzillaClient(webLocation2, repository2, connector);
			System.out.println(webLocation2.getCredentials(AuthenticationType.REPOSITORY).getUserName());
			clients[i].authenticate(new NullProgressMonitor());
		}

		createTestFiles();
		for (int i = 0; i < 10000; i++) {
			x(client, repository, connector, url);
		}
		System.exit(0);
		
		BugzillaAttributeMapper mapper = new BugzillaAttributeMapper(repository, connector);
		TaskData newData = new TaskData(mapper, BugzillaCorePlugin.CONNECTOR_KIND, url, "");

		if (!connector.getTaskDataHandler().initializeTaskData(repository, newData, null,
				new NullProgressMonitor())) {
			System.out.println("Failed to initialize task");
		}

		long timestamp = System.currentTimeMillis();

		final String user1 = "wseliga@atlassian.com";
		final String user2 = "piotr.maruszak@spartez.com";

		newData.getRoot().getMappedAttribute(TaskAttribute.SUMMARY).setValue("wseliga - testing " + timestamp);
		newData.getRoot().getMappedAttribute(TaskAttribute.PRODUCT).setValue("TestProduct");

		// component
		newData.getRoot().getMappedAttribute(TaskAttribute.COMPONENT).setValue("TestComponent");

		// version
		newData.getRoot().getMappedAttribute(BugzillaAttribute.VERSION.getKey()).setValue("1.0");
		newData.getRoot().getMappedAttribute(BugzillaAttribute.OP_SYS.getKey()).setValue("All");
		newData.getRoot().getMappedAttribute(TaskAttribute.SEVERITY).setValue("blocker");
		newData.getRoot().getMappedAttribute(TaskAttribute.PRIORITY).setValue("high");
		newData.getRoot().getMappedAttribute(TaskAttribute.DESCRIPTION).setValue("" + timestamp);

		// watchers
		newData.getRoot().getMappedAttribute(BugzillaAttribute.NEWCC.getKey()).setValue(user1 + ", " + user2);

		// votes
		// newData.getRoot().getMappedAttribute(BugzillaAttribute.VOTES.getKey()).setValue(user1);

		// assignee
		newData.getRoot().getMappedAttribute(BugzillaAttribute.ASSIGNED_TO.getKey()).setValue(user1);

		// links
		newData.getRoot().getMappedAttribute(BugzillaAttribute.DEPENDSON.getKey()).setValue("20");
		newData.getRoot().getMappedAttribute(BugzillaAttribute.BLOCKED.getKey()).setValue("19");

		// status
		newData.getRoot().getMappedAttribute(BugzillaAttribute.BUG_STATUS.getKey()).setValue("ASSIGNED");


		RepositoryResponse response = client.postTaskData(newData, new NullProgressMonitor());
		System.out.println(response.getReposonseKind() + " " + response.getTaskId());

		final TaskData[] createdTask = new TaskData[1];
		client.getTaskData(Collections.singleton(response.getTaskId()), new TaskDataCollector() {

			@Override
			public void accept(TaskData taskData) {
				createdTask[0] = taskData;

			}
		}, mapper, new NullProgressMonitor());

		createdTask[0].getRoot().getMappedAttribute(BugzillaAttribute.BUG_STATUS.getKey()).setValue("TESTING");
		// worklog
		BugzillaTaskDataHandler.createAttribute(createdTask[0].getRoot(), BugzillaAttribute.WORK_TIME);
		createdTask[0].getRoot().getMappedAttribute(BugzillaAttribute.WORK_TIME.getKey()).setValue("10");
		createdTask[0].getRoot().getMappedAttribute(BugzillaAttribute.NEW_COMMENT.getKey())
				.setValue("my reason for adding work");

		response = client.postTaskData(createdTask[0], new NullProgressMonitor());
		System.out.println(response.getReposonseKind() + " " + response.getTaskId());

		// duplicate
		client.getTaskData(Collections.singleton(response.getTaskId()), new TaskDataCollector() {

			@Override
			public void accept(TaskData taskData) {
				createdTask[0] = taskData;

			}
		}, mapper, new NullProgressMonitor());

		createdTask[0].getRoot().getMappedAttribute(BugzillaAttribute.BUG_STATUS.getKey()).setValue("RESOLVED");
		createdTask[0].getRoot().getMappedAttribute(BugzillaAttribute.RESOLUTION.getKey()).setValue("DUPLICATE");
		BugzillaTaskDataHandler.createAttribute(createdTask[0].getRoot(), BugzillaAttribute.DUP_ID);
		createdTask[0].getRoot().getMappedAttribute(BugzillaAttribute.DUP_ID.getKey()).setValue("16");
		BugzillaTaskDataHandler.createAttribute(createdTask[0].getRoot(), BugzillaAttribute.WORK_TIME);
		createdTask[0].getRoot().getMappedAttribute(BugzillaAttribute.WORK_TIME.getKey()).setValue("20");

		// target milestone
		createdTask[0].getRoot().getMappedAttribute(BugzillaAttribute.TARGET_MILESTONE.getKey()).setValue("M2");
		createdTask[0].getRoot().getMappedAttribute(BugzillaAttribute.NEW_COMMENT.getKey())
				.setValue("another reason for adding work");

		response = client.postTaskData(createdTask[0], new NullProgressMonitor());
		System.out.println(response.getReposonseKind() + " " + response.getTaskId());

		attachFile(client, new File("/tmp/surefire2323378835174320423tmp"), createdTask[0]);

		System.exit(0);
	}

	private static Random random = new Random();

	private static void x(BugzillaClient client, TaskRepository repository, BugzillaRepositoryConnector connector, String url)
			throws Exception {

		BugzillaAttributeMapper mapper = new BugzillaAttributeMapper(repository, connector);
		TaskData newData = new TaskData(mapper, BugzillaCorePlugin.CONNECTOR_KIND, url, "");

		if (!connector.getTaskDataHandler().initializeTaskData(repository, newData, null,
				new NullProgressMonitor())) {
			System.out.println("Failed to initialize task");
		}

		final int userNum = random.nextInt(7);

		StringBuilder sb = new StringBuilder("test issue ");

		newData.getRoot().getMappedAttribute(TaskAttribute.PRODUCT).setValue("TestProduct");

		// assignee
		if (userNum != 0) {
			final String assignee = "user" + userNum + "@example.com";
			newData.getRoot().getMappedAttribute(BugzillaAttribute.ASSIGNED_TO.getKey()).setValue(assignee);
			sb.append(" ,assigned to ").append(assignee);
		} else {
			sb.append(" unassigned");
		}

		// component
		final int componentNum = random.nextInt(4);
		newData.getRoot().getMappedAttribute(TaskAttribute.COMPONENT).setValue("Component" + (componentNum + 1));

		// version
		final int versionNum = random.nextInt(5);
		if (versionNum != 0) {
			final String version = "" + (versionNum) + ".0";
			newData.getRoot().getMappedAttribute(BugzillaAttribute.VERSION.getKey()).setValue(version);
			sb.append(" ,version: ").append(version);
		} else {
			sb.append(" ,without version");
		}

		newData.getRoot().getMappedAttribute(BugzillaAttribute.OP_SYS.getKey()).setValue("All");
		
		String[] severities = new String[] { "blocker", "critical", "major", "normal", "minor", "trivial", "enhancement" };
		String[] priorities = new String[] { "Highest", "High", "Normal", "Low", "Lowest", "---" };
      
		final String severity = severities[random.nextInt(severities.length)];
		newData.getRoot().getMappedAttribute(TaskAttribute.SEVERITY).setValue(severity);
		sb.append(" ,severity: " + severity);

		final String priority = priorities[random.nextInt(priorities.length)];
		newData.getRoot().getMappedAttribute(TaskAttribute.PRIORITY).setValue(priority);
		sb.append(" ,priority: " + priority);


		int numWatchers = random.nextInt(6);
		if (numWatchers != 0) {
			StringBuilder ccUsers = new StringBuilder();
			for (int i = 0; i < numWatchers; i++) {
				ccUsers.append("user" + (i + 1) + "@example.com, ");
			}
			// watchers
			newData.getRoot().getMappedAttribute(BugzillaAttribute.NEWCC.getKey()).setValue(ccUsers.toString());
		}

		final int dependsOnNum = random.nextInt(5);
		if (dependsOnNum != 0) {
			String dependsOn = getNumSequence(10, dependsOnNum);
			// links
			newData.getRoot().getMappedAttribute(BugzillaAttribute.DEPENDSON.getKey()).setValue(dependsOn);
			sb.append(", depends on: " + dependsOn);
		}

		final int blocksNum = random.nextInt(5);
		if (blocksNum != 0) {
			String blocks = getNumSequence(20, blocksNum);
			newData.getRoot().getMappedAttribute(BugzillaAttribute.BLOCKED.getKey()).setValue(blocks);
			sb.append(", blocks: " + blocks);
		}

		// status
		if (random.nextBoolean()) {
			newData.getRoot().getMappedAttribute(BugzillaAttribute.BUG_STATUS.getKey()).setValue("ASSIGNED");
			sb.append(", status: assigned");
		} else {
			sb.append(", status: new");
		}

		newData.getRoot().getMappedAttribute(TaskAttribute.DESCRIPTION)
				.setValue("My description" + new Date() + "\n" + sb.toString());

		newData.getRoot().getMappedAttribute(TaskAttribute.SUMMARY).setValue(sb.toString());

		System.out.println(sb.toString());
		RepositoryResponse response = client.postTaskData(newData, new NullProgressMonitor());
		System.out.println(response.getReposonseKind() + " " + response.getTaskId());

		// -----------

		final TaskData[] createdTask = new TaskData[1];
		client.getTaskData(Collections.singleton(response.getTaskId()), new TaskDataCollector() {

			@Override
			public void accept(TaskData taskData) {
				createdTask[0] = taskData;

			}
		}, mapper, new NullProgressMonitor());

		StringBuilder comment = new StringBuilder();

		final int newStatus = random.nextInt(4);
		if (newStatus == 1) {
			if (!"ASSIGNED".equals(createdTask[0].getRoot().getMappedAttribute(BugzillaAttribute.BUG_STATUS.getKey())
					.getValue())) {
				createdTask[0].getRoot().getMappedAttribute(BugzillaAttribute.BUG_STATUS.getKey()).setValue("ASSIGNED");
				comment.append("setting to ASSIGNED");

			} else {
				createdTask[0].getRoot().getMappedAttribute(BugzillaAttribute.BUG_STATUS.getKey()).setValue("TESTING");
				comment.append("setting to TESTING");
			}
		} else if (newStatus == 2) {
			createdTask[0].getRoot().getMappedAttribute(BugzillaAttribute.BUG_STATUS.getKey()).setValue("RESOLVED");
			createdTask[0].getRoot().getMappedAttribute(BugzillaAttribute.RESOLUTION.getKey()).setValue("FIXED");
			comment.append("setting to RESOLVED & FIXED");
		} else if (newStatus == 3) {
			// duplicate
			final String dupeId = "" + (random.nextInt(30) + 100);
			createdTask[0].getRoot().getMappedAttribute(BugzillaAttribute.BUG_STATUS.getKey()).setValue("RESOLVED");
			createdTask[0].getRoot().getMappedAttribute(BugzillaAttribute.RESOLUTION.getKey()).setValue("DUPLICATE");
			BugzillaTaskDataHandler.createAttribute(createdTask[0].getRoot(), BugzillaAttribute.DUP_ID);
			createdTask[0].getRoot().getMappedAttribute(BugzillaAttribute.DUP_ID.getKey()).setValue(dupeId);
			comment.append("setting to RESOLVED, DUPE OF " + dupeId);
		}

		if (random.nextBoolean()) {
			// target milestone
			final String targetMilestone = "M" + (1 + random.nextInt(5));
			createdTask[0].getRoot().getMappedAttribute(BugzillaAttribute.TARGET_MILESTONE.getKey()).setValue("M2");
			comment.append(" and setting target milestone to " + targetMilestone);
		}

		// worklog
		BugzillaTaskDataHandler.createAttribute(createdTask[0].getRoot(), BugzillaAttribute.WORK_TIME);
		createdTask[0].getRoot().getMappedAttribute(BugzillaAttribute.WORK_TIME.getKey())
				.setValue("" + (1 + random.nextInt(50)));
		createdTask[0].getRoot().getMappedAttribute(BugzillaAttribute.NEW_COMMENT.getKey())
				.setValue(comment.toString() + "\nmy reason for adding work " + new Date().getTime());


		response = client.postTaskData(createdTask[0], new NullProgressMonitor());
		System.out.println(response.getReposonseKind() + " " + response.getTaskId());

		// adding more worklog
		final int numWorklogEntries = random.nextInt(5);
		System.out.println("Adding " + numWorklogEntries + " worklog entries");
		for (int i = 0; i < numWorklogEntries; i++) {
			final int clientUsed = random.nextInt(clients.length);
			clients[clientUsed].getTaskData(Collections.singleton(response.getTaskId()), new TaskDataCollector() {

				@Override
				public void accept(TaskData taskData) {
					createdTask[0] = taskData;

				}
			}, mapper, new NullProgressMonitor());

			BugzillaTaskDataHandler.createAttribute(createdTask[0].getRoot(), BugzillaAttribute.WORK_TIME);
			createdTask[0].getRoot().getMappedAttribute(BugzillaAttribute.WORK_TIME.getKey())
					.setValue("" + (random.nextInt(10) + 1));

			createdTask[0].getRoot().getMappedAttribute(BugzillaAttribute.NEW_COMMENT.getKey())
					.setValue("another reason for adding work " + System.nanoTime());

			response = clients[clientUsed].postTaskData(createdTask[0], new NullProgressMonitor());
			System.out.println(response.getReposonseKind() + " " + response.getTaskId());
		}

		final int numAttachments = random.nextInt(10) - 5;
		if (numAttachments > 0) {
			System.out.println("Attaching " + numAttachments + " files");
			for (int i = 0; i < numAttachments; i++) {
				final int clientUsed = random.nextInt(clients.length);
				attachFile(clients[clientUsed], testFiles[i], createdTask[0]);
			}
		}

		// attachFile(client, new File("/tmp/surefire2323378835174320423tmp"), createdTask[0]);

	}

	private static String getNumSequence(int start, int numEntries) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < numEntries; i++) {
			sb.append(i + start);
			if (i != numEntries - 1) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	private static void attachFile(BugzillaClient client, File file, TaskData taskData) throws Exception {
		TaskAttribute attrAttachment = taskData.getAttributeMapper().createTaskAttachment(taskData);
		TaskAttachmentMapper attachmentMapper = TaskAttachmentMapper.createFrom(attrAttachment);

		attachmentMapper.setDescription("Test attachment " + new Date());
		attachmentMapper.setContentType("text/plain");
		attachmentMapper.setPatch(false);
		attachmentMapper.setComment("Automated attachment test");
		attachmentMapper.applyTo(attrAttachment);

		FileTaskAttachmentSource attachment = new FileTaskAttachmentSource(file);
		attachment.setContentType(FileTaskAttachmentSource.APPLICATION_OCTET_STREAM);
		attachment.setDescription("my description");
		attachment.setName(file.getName());
		client.postAttachment(taskData.getTaskId(), attachmentMapper.getComment(), attachment, attrAttachment,
				new NullProgressMonitor());
		System.out.println("Attached file " + file.getName());

	}

	private static File[] testFiles;

	private static void createTestFiles() {
		testFiles = new File[4];
		for (int i = 0; i < testFiles.length; i++) {
			try {
				testFiles[i] = File.createTempFile("bugzilla", "test");
				testFiles[i].deleteOnExit();

				FileWriter fileWriter = new FileWriter(testFiles[i]);

				for (int b = 0; b < 100 + 1000 * i; b++) {
					fileWriter.write("ABC");
				}
				fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
