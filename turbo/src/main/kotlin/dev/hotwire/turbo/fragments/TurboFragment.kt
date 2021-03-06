package dev.hotwire.turbo.fragments

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import dev.hotwire.turbo.R
import dev.hotwire.turbo.config.context
import dev.hotwire.turbo.config.title
import dev.hotwire.turbo.delegates.TurboFragmentDelegate
import dev.hotwire.turbo.nav.TurboNavDestination
import dev.hotwire.turbo.nav.TurboNavPresentationContext
import dev.hotwire.turbo.session.TurboSessionModalResult

/**
 * The base class from which all "standard" native Fragments (non-dialogs) in a
 * Turbo-driven app should extend from.
 *
 * For web fragments, refer to [TurboWebFragment].
 */
abstract class TurboFragment : Fragment(), TurboNavDestination {
    internal lateinit var delegate: TurboFragmentDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate = TurboFragmentDelegate(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeModalResult()
        observeDialogResult()

        if (shouldObserveTitleChanges()) {
            observeTitleChanges()
            pathProperties.title?.let {
                fragmentViewModel.setTitle(it)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        delegate.onActivityCreated()
    }

    override fun onStart() {
        super.onStart()

        if (!delegate.sessionViewModel.modalResultExists) {
            delegate.onStart()
        }
    }

    override fun onStop() {
        super.onStop()
        delegate.onStop()
    }

    /**
     * Called when the Fragment has been started again after receiving a
     * modal result. Will navigate if the result indicates it should.
     */
    open fun onStartAfterModalResult(result: TurboSessionModalResult) {
        delegate.onStartAfterModalResult(result)
    }

    /**
     * Called when the Fragment has been started again after a dialog has
     * been dismissed/canceled and no result is passed back.
     */
    open fun onStartAfterDialogCancel() {
        if (!delegate.sessionViewModel.modalResultExists) {
            delegate.onStartAfterDialogCancel()
        }
    }

    override fun onBeforeNavigation() {}

    /**
     * Gets the Toolbar instance in your Fragment's view for use with
     * navigation. The title in the Toolbar will automatically be
     * updated if a title is available. By default, Turbo will look
     * for a Toolbar with resource ID `R.id.toolbar`. Override to
     * provide a Toolbar instance with a different ID.
     */
    override fun toolbarForNavigation(): Toolbar? {
        return view?.findViewById(R.id.toolbar)
    }

    final override fun delegate(): TurboFragmentDelegate {
        return delegate
    }

    private fun observeModalResult() {
        delegate.sessionViewModel.modalResult.observe(viewLifecycleOwner) { event ->
            if (shouldHandleModalResults()) {
                event.getContentIfNotHandled()?.let {
                    onStartAfterModalResult(it)
                }
            }
        }
    }

    private fun observeDialogResult() {
        delegate.sessionViewModel.dialogResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                onStartAfterDialogCancel()
            }
        }
    }

    private fun observeTitleChanges() {
        fragmentViewModel.title.observe(viewLifecycleOwner) {
            toolbarForNavigation()?.title = it
        }
    }

    private fun shouldHandleModalResults(): Boolean {
        // Only handle modal results in non-modal contexts
        return pathProperties.context != TurboNavPresentationContext.MODAL
    }
}
