package dev.hotwire.turbo.nav

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import dev.hotwire.turbo.R
import dev.hotwire.turbo.config.TurboPathConfiguration
import dev.hotwire.turbo.config.TurboPathConfigurationProperties
import dev.hotwire.turbo.delegates.TurboFragmentDelegate
import dev.hotwire.turbo.fragments.TurboFragmentViewModel
import dev.hotwire.turbo.session.TurboSession
import dev.hotwire.turbo.session.TurboSessionNavHostFragment
import dev.hotwire.turbo.delegates.TurboNestedFragmentDelegate
import dev.hotwire.turbo.visit.TurboVisitOptions

/**
 * The primary interface that a navigable Fragment implements to provide the library with
 * the information it needs to properly navigate.
 */
interface TurboNavDestination {
    /**
     * Gets the fragment instance for this destination.
     */
    val fragment: Fragment
        get() = this as Fragment

    /**
     * Gets the Turbo session's nav host fragment associated with this destination.
     */
    val sessionNavHostFragment: TurboSessionNavHostFragment
        get() = fragment.parentFragment as TurboSessionNavHostFragment

    /**
     * Gets the location for this destination.
     */
    val location: String
        get() = requireNotNull(fragment.arguments?.location)

    /**
     * Gets the previous back stack entry's location from the nav controller.
     */
    val previousLocation: String?
        get() = navController()?.previousBackStackEntry?.arguments?.location

    /**
     * Gets the path configuration properties for the location associated with this
     * destination.
     */
    val pathProperties: TurboPathConfigurationProperties
        get() = pathConfiguration.properties(location)

    /**
     * Gets the [TurboSession] associated with this destination.
     */
    val session: TurboSession
        get() = sessionNavHostFragment.session

    /**
     * Gets the [TurboFragmentViewModel] associated with this destination.
     */
    val fragmentViewModel: TurboFragmentViewModel
        get() = delegate().fragmentViewModel

    /**
     * Gets the delegate instance that handles the Fragment's lifecycle events.
     */
    fun delegate(): TurboFragmentDelegate

    /**
     * Returns the [Toolbar] used for navigation by the given view.
     */
    fun toolbarForNavigation(): Toolbar?

    /**
     * Specifies whether title changes should be automatically observed and update
     * the title in the Toolbar provided from toolbarForNavigation(), if available.
     * Default is true.
     */
    fun shouldObserveTitleChanges(): Boolean {
        return true
    }

    /**
     * Called before any navigation action takes places. This is a useful place
     * for state cleanup in your Fragment if necessary.
     */
    fun onBeforeNavigation()

    /**
     * Gets the nav host fragment that will be used for navigating to `newLocation`. You should
     * not have to override this, unless you're using a [TurboNestedFragmentDelegate] to provide
     * sub-navigation within your current Fragment destination and would like custom behavior.
     */
    fun navHostForNavigation(newLocation: String): TurboSessionNavHostFragment {
        return sessionNavHostFragment
    }

    /**
     * Gets whether the new location should be navigated to from the current destination. Override
     * to provide your own custom rules based on the location's domain, protocol, path, or any other
     * factors. (e.g. external domain urls or mailto: links should not be sent through the normal
     * Turbo navigation flow).
     */
    fun shouldNavigateTo(newLocation: String): Boolean {
        return true
    }

    /**
     * Navigates to the specified location. The resulting destination and its presentation
     * will be determined using the path configuration rules.
     *
     * @param location The location to navigate to.
     * @param options Visit options to apply to the visit. (optional)
     * @param bundle Bundled arguments to pass to the destination. (optional)
     * @param extras Extras that can be passed to enable Fragment specific behavior. (optional)
     */
    fun navigate(
        location: String,
        options: TurboVisitOptions = TurboVisitOptions(),
        bundle: Bundle? = null,
        extras: FragmentNavigator.Extras? = null
    ) {
        navigator.navigate(location, options, bundle, extras)
    }

    /**
     * Gets the default set of navigation options (basic enter/exit animations) for the Android
     * Navigation component to use to execute a navigation event. This can be overridden if
     * you'd like to provide your own.
     */
    fun getNavigationOptions(
        newLocation: String,
        newPathProperties: TurboPathConfigurationProperties
    ): NavOptions {
        return navOptions {
            anim {
                enter = R.anim.nav_default_enter_anim
                exit = R.anim.nav_default_exit_anim
                popEnter = R.anim.nav_default_pop_enter_anim
                popExit = R.anim.nav_default_pop_exit_anim
            }
        }
    }

    /**
     * Navigates up to the previous destination. See [NavController.navigateUp] for
     * more details.
     */
    fun navigateUp() {
        navigator.navigateUp()
    }

    /**
     * Navigates back to the previous destination. See [NavController.popBackStack] for
     * more details.
     */
    fun navigateBack() {
        navigator.navigateBack()
    }

    /**
     * Clears the navigation back stack to the start destination.
     */
    fun clearBackStack(onCleared: () -> Unit = {}) {
        navigator.clearBackStack(onCleared)
    }

    /**
     * Finds the nav host fragment with the given resource ID.
     */
    fun findNavHostFragment(@IdRes navHostFragmentId: Int): TurboSessionNavHostFragment {
        return fragment.parentFragment?.childFragmentManager?.findNavHostFragment(navHostFragmentId)
            ?: fragment.parentFragment?.parentFragment?.childFragmentManager?.findNavHostFragment(navHostFragmentId)
            ?: fragment.requireActivity().supportFragmentManager.findNavHostFragment(navHostFragmentId)
            ?: throw IllegalStateException("No TurboSessionNavHostFragment found with ID: $navHostFragmentId")
    }

    private val Bundle.location
        get() = getString("location")

    private val navigator: TurboNavigator
        get() = delegate().navigator

    private val pathConfiguration: TurboPathConfiguration
        get() = session.pathConfiguration

    /**
     * Retrieve the nav controller indirectly from the parent NavHostFragment,
     * since it's only available when the fragment is attached to its parent.
     */
    private fun navController(): NavController? {
        return fragment.parentFragment?.findNavController()
    }

    private fun FragmentManager.findNavHostFragment(navHostFragmentId: Int): TurboSessionNavHostFragment? {
        return findFragmentById(navHostFragmentId) as? TurboSessionNavHostFragment
    }
}
