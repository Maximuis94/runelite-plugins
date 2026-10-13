package com.activitycounter.ui;

import com.activitycounter.ActivityCounterPlugin;
import com.activitycounter.models.Count;
import com.activitycounter.models.TrackedActivity;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

/**
 * Overlay panel that may be used to display a subset of activities ingame
 */
public class ActivityCounterOverlay extends OverlayPanel
{
	private final ActivityCounterPlugin plugin;

	@Inject
	private ActivityCounterOverlay(ActivityCounterPlugin plugin)
	{
		super(plugin);
		this.plugin = plugin;
		setPosition(OverlayPosition.TOP_LEFT);
		setMinimumSize(200);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.getPinnedActivityIds().isEmpty() || plugin.getCurrentSession() == null)
		{
			return null;
		}

		for (int activityId : plugin.getPinnedActivityIds())
		{
			TrackedActivity activity = TrackedActivity.getByStorageId(activityId);
			if (activity == null) continue;

			Count count = plugin.getCurrentSession().getKillCount(activityId);
			int displayValue = (count != null) ? count.getSessionKc() : 0;
			panelComponent.getChildren().add(LineComponent.builder()
				.left(activity.getName())
				.right(Integer.toString(displayValue))
				.build());
		}
		return super.render(graphics);
	}
}