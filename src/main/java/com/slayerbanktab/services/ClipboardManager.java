package com.slayerbanktab.services;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ClipboardManager
{
	/**
	 * Retrieves and trims text from the system clipboard.
	 * Returns null if the clipboard is empty, inaccessible, or does not contain text.
	 */
	public String getText()
	{
		try
		{
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor))
			{
				String data = (String) clipboard.getData(DataFlavor.stringFlavor);
				if (data != null && !data.trim().isEmpty())
				{
					return data.trim();
				}
			}
		}
		catch (UnsupportedFlavorException | IOException | IllegalStateException ex)
		{
			log.warn("Failed to read text from clipboard", ex);
		}

		return null;
	}

	/**
	 * Copies the provided text to the system clipboard.
	 * * @param text The text to copy. If null or empty, the operation is ignored.
	 */
	public void setText(String text)
	{
		if (text == null || text.isEmpty())
		{
			return;
		}

		try
		{
			StringSelection selection = new StringSelection(text);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
			log.debug("Set clipboard text to '{}'", text);
		}
		catch (IllegalStateException ex)
		{
			log.warn("Failed to write text to clipboard. Clipboard might be unavailable.", ex);
		}
	}
}