package che.incubator.dashboard

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.RightAlignedToolbarAction
import com.intellij.openapi.actionSystem.ex.TooltipDescriptionProvider
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

class OpenDashboardAction : AnAction(), RightAlignedToolbarAction, TooltipDescriptionProvider {

    private val dashboardUrl: String? = System.getenv("CHE_DASHBOARD_URL")
    private val actionEnabled = dashboardUrl.let { !it.isNullOrEmpty() }

    override fun actionPerformed(e: AnActionEvent) {
        if (!dashboardUrl.isNullOrEmpty()) {
            BrowserUtil.browse(dashboardUrl)
        }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)

        val presentation = e.presentation
        if (e.isFromActionToolbar) presentation.text = ""
        presentation.description = getActionTooltip()
        presentation.icon = getActionIcon()
        presentation.isEnabled = actionEnabled
        presentation.isVisible = actionEnabled
    }

    private fun getActionTooltip(): String {
        return "Open Dashboard"
    }

    private fun getActionIcon(): Icon {
        return IconLoader.getIcon("/dashboard.svg", OpenDashboardAction::class.java)
    }
}
