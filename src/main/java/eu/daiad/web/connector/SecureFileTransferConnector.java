package eu.daiad.web.connector;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import eu.daiad.web.model.error.ApplicationException;

@Service
public class SecureFileTransferConnector {

	public ArrayList<RemoteFileAttributes> ls(SftpProperties properties, String path) {
		Session session = null;
		Channel channel = null;
		ChannelSftp channelSftp = null;

		try {
			JSch jsch = new JSch();
			session = jsch.getSession(properties.getUsername(), properties.getHost(), properties.getPort());
			session.setPassword(properties.getPassword());

			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);

			session.connect();

			channel = session.openChannel("sftp");
			channel.connect();

			channelSftp = (ChannelSftp) channel;
			Vector<?> entries = channelSftp.ls(path);

			ArrayList<RemoteFileAttributes> filenames = new ArrayList<RemoteFileAttributes>();
			for (Object entry : entries) {
				LsEntry lsEntry = (LsEntry) entry;
				if (!StringUtils.isBlank(FilenameUtils.normalize(lsEntry.getFilename()))) {
					filenames.add(new RemoteFileAttributes(properties.getHost(), path, lsEntry.getFilename(), lsEntry
									.getAttrs().getSize(), lsEntry.getAttrs().getMTime()));
				}
			}

			return filenames;
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex).set("path", path);
		} finally {

			channelSftp.exit();
			channelSftp = null;

			channel.disconnect();
			channel = null;

			session.disconnect();
			session = null;
		}
	}

	public void get(SftpProperties properties, String path, String filename, String target) {
		Session session = null;
		Channel channel = null;
		ChannelSftp channelSftp = null;

		try {
			JSch jsch = new JSch();
			session = jsch.getSession(properties.getUsername(), properties.getHost(), properties.getPort());
			session.setPassword(properties.getPassword());

			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);

			session.connect();

			channel = session.openChannel("sftp");
			channel.connect();

			channelSftp = (ChannelSftp) channel;
			channelSftp.cd(path);

			InputStream input = channelSftp.get(filename);

			File output = new File(target);

			FileUtils.copyInputStreamToFile(input, output);
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex).set("path", path).set("filename", filename);
		} finally {
			channelSftp.exit();
			channelSftp = null;

			channel.disconnect();
			channel = null;

			session.disconnect();
			session = null;
		}
	}
}
