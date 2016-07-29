/**
 * Copyright (C) 2010-2016 Structr GmbH
 *
 * This file is part of Structr <http://structr.org>.
 *
 * Structr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Structr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Structr.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.files.ftp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.ftpserver.ftplet.FtpFile;
import org.structr.common.error.FrameworkException;
import static org.structr.core.GraphObject.lastModifiedDate;
import org.structr.core.app.App;
import org.structr.core.app.StructrApp;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.AbstractUser;
import org.structr.core.entity.Principal;
import org.structr.core.graph.Tx;
import org.structr.web.Importer;
import org.structr.web.common.RenderContext;
import org.structr.web.diff.InvertibleModificationOperation;
import org.structr.web.entity.FileBase;
import org.structr.web.entity.dom.Page;

/**
 *
 */
public class FtpFilePageWrapper implements FtpFile {

	private static final Logger logger = Logger.getLogger(FtpFilePageWrapper.class.getName());

	private Page page = null;

	public FtpFilePageWrapper(final Page page) {
		this.page = page;
	}

	// ----- interface FtpFile -----
	@Override
	public String getAbsolutePath() {

		try (Tx tx = StructrApp.getInstance().tx()) {

			final String path = page.getPath();
			tx.success();

			return path;

		} catch (FrameworkException fex) {
			logger.log(Level.SEVERE, "Error in getPath() of abstract ftp file", fex);
		}
		return null;
	}

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public boolean isFile() {
		return true;
	}

	@Override
	public boolean doesExist() {
		return true;
	}

	@Override
	public boolean isReadable() {
		return true;
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public boolean isRemovable() {
		return true;
	}

	private Principal getOwner() {

		try (Tx tx = StructrApp.getInstance().tx()) {

			Principal owner = page.getProperty(FileBase.owner);
			tx.success();

			return owner;

		} catch (FrameworkException fex) {
			logger.log(Level.SEVERE, "Error while getting owner of " + this, fex);
		}
		return null;
	}

	@Override
	public String getOwnerName() {

		String name = "";

		try (Tx tx = StructrApp.getInstance().tx()) {

			Principal owner = getOwner();
			if (owner != null) {

				name = owner.getProperty(AbstractUser.name);
			}
			tx.success();

		} catch (FrameworkException fex) {
			logger.log(Level.SEVERE, "Error while getting owner name of " + this, fex);
		}

		return name;
	}

	@Override
	public String getGroupName() {

		String name = "";

		try (Tx tx = StructrApp.getInstance().tx()) {

			Principal owner = getOwner();

			if (owner != null) {
				List<Principal> parents = owner.getParents();
				if (!parents.isEmpty()) {

					name = parents.get(0).getProperty(AbstractNode.name);
				}
			}

			tx.success();

		} catch (FrameworkException fex) {
			logger.log(Level.SEVERE, "Error while getting group name of " + this, fex);
		}

		return name;
	}

	@Override
	public int getLinkCount() {
		return 1;
	}

	@Override
	public long getLastModified() {

		long lastModified = 0L;

		try (Tx tx = StructrApp.getInstance().tx()) {

			lastModified = page.getProperty(lastModifiedDate).getTime();
			tx.success();

		} catch (FrameworkException fex) {
			logger.log(Level.SEVERE, "Error while last modified date of " + this, fex);
		}

		return lastModified;
	}

	@Override
	public boolean setLastModified(long time) {

		try (Tx tx = StructrApp.getInstance().tx()) {

			page.setProperty(lastModifiedDate, new Date(time));
			tx.success();

		} catch (FrameworkException ex) {
			logger.log(Level.SEVERE, null, ex);
		}

		return true;
	}

	@Override
	public long getSize() {

		long size = 0L;

		try (Tx tx = StructrApp.getInstance().tx()) {

			size = page.getContent(RenderContext.EditMode.RAW).length();
			tx.success();

		} catch (FrameworkException fex) {
			logger.log(Level.SEVERE, "Error while last modified date of " + this, fex);
		}

		return size;
	}

	@Override
	public String getName() {

		String name = null;

		try (Tx tx = StructrApp.getInstance().tx()) {

			name = page.getProperty(AbstractNode.name);
			tx.success();

		} catch (FrameworkException fex) {
			logger.log(Level.SEVERE, "Error in getName() of page", fex);
		}

		return name;
	}

	@Override
	public boolean isHidden() {

		boolean hidden = true;

		try (Tx tx = StructrApp.getInstance().tx()) {

			hidden = page.getProperty(Page.hidden);
			tx.success();

		} catch (FrameworkException fex) {
			logger.log(Level.SEVERE, "Error in isHidden() of page", fex);
		}

		return hidden;
	}

	@Override
	public boolean mkdir() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean delete() {
		final App app = StructrApp.getInstance();

		try (Tx tx = StructrApp.getInstance().tx()) {

			app.delete(page);

			tx.success();

		} catch (FrameworkException ex) {
			logger.log(Level.SEVERE, null, ex);
		}

		return true;
	}

	@Override
	public boolean move(FtpFile target) {
		try (Tx tx = StructrApp.getInstance().tx()) {

			logger.log(Level.INFO, "move()");

			final AbstractStructrFtpFile targetFile = (AbstractStructrFtpFile) target;
			final String path = targetFile instanceof StructrFtpFile ? "/" : targetFile.getAbsolutePath();

			try {

				if (!("/".equals(path))) {
					final String newName = path.contains("/") ? StringUtils.substringAfterLast(path, "/") : path;
					page.setProperty(AbstractNode.name, newName);
				}

			} catch (FrameworkException ex) {
				logger.log(Level.SEVERE, "Could not move ftp file", ex);
				return false;
			}

			tx.success();

			return true;
		} catch (FrameworkException ex) {
			logger.log(Level.SEVERE, null, ex);
		}

		return false;
	}

	@Override
	public List<FtpFile> listFiles() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public OutputStream createOutputStream(long offset) throws IOException {

		final Page origPage = page;

		OutputStream out = new ByteArrayOutputStream() {

			@Override
			public void flush() throws IOException {

				final String source = toString();

				final App app = StructrApp.getInstance();
				try (Tx tx = app.tx()) {

					// parse page from modified source
					Page modifiedPage = Importer.parsePageFromSource(page.getSecurityContext(), source, "__FTP_Temporary_Page__");

					final List<InvertibleModificationOperation> changeSet = Importer.diffNodes(origPage, modifiedPage);

					for (final InvertibleModificationOperation op : changeSet) {

						// execute operation
						op.apply(app, origPage, modifiedPage);

					}

					app.delete(modifiedPage);

					tx.success();

				} catch (FrameworkException fex) {
					logger.log(Level.WARNING, "", fex);
				}

				super.flush();

			}

		};

		return out;
	}

	@Override
	public InputStream createInputStream(long offset) throws IOException {

		ByteArrayInputStream bis = null;
		try (Tx tx = StructrApp.getInstance().tx()) {

			bis = new ByteArrayInputStream(page.getContent(RenderContext.EditMode.RAW).getBytes("UTF-8"));
			tx.success();

		} catch (FrameworkException fex) {
			logger.log(Level.WARNING, "", fex);
		}

		return bis;
	}
}