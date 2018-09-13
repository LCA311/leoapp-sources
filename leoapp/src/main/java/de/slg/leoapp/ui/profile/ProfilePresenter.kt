package de.slg.leoapp.ui.profile

import android.graphics.Bitmap
import de.slg.leoapp.core.data.ProfilePicture
import de.slg.leoapp.core.data.User
import de.slg.leoapp.core.ui.mvp.AbstractPresenter
import de.slg.leoapp.data.IUserDataManager

class ProfilePresenter : AbstractPresenter<ProfileView, IUserDataManager>(), IProfilePresenter {

    private var editing: Boolean = false
    private lateinit var user: User
    private lateinit var currentProfilePicture: Bitmap

    override fun onViewAttached(view: ProfileView) {
        super.onViewAttached(view)
        user = User(getMvpView().getViewContext())
        getMvpView().setName("${user.firstName} ${user.lastName}")
        getMvpView().setGrade(user.grade)
        getMvpView().setLoginName(user.loginName)

        currentProfilePicture = user.profilePicture.getPictureOrPlaceholder()
        getMvpView().setProfilePicture(currentProfilePicture)
    }

    override fun onBackPressed() {
        if (editing) {
            getMvpView().disableTextViewEditing()
            getMvpView().hideImageViewEditOverlay()
            getMvpView().setName("${user.firstName} ${user.lastName}")
            getMvpView().setProfilePicture(user.profilePicture.getPictureOrPlaceholder())
        } else {
            getMvpView().terminate()
        }
    }

    override fun onEditStarted() {
        editing = true
        getMvpView().enableTextViewEditing()
        getMvpView().showImageViewEditOverlay()
    }

    override fun onEditFinished() {
        val enteredNames = getMvpView().getName().split(" ")

        if (enteredNames.size < 2) {
            getMvpView().showInvalidNameError()
            return
        }

        user.firstName = with (enteredNames) { slice(0..size-2).joinToString(separator = " ") }
        user.lastName = enteredNames[enteredNames.size-1]
        user.profilePicture = ProfilePicture(currentProfilePicture, user.id)

        getDataManager().updateUsername(user.firstName, user.lastName)
        getDataManager().updateProfilePicture(currentProfilePicture)

        getMvpView().disableTextViewEditing()
        getMvpView().hideImageViewEditOverlay()
        editing = false
    }

    override fun onImageInteraction() {
        if (editing) {
            getMvpView().openImageSelectionDialog()
        }
    }

    override fun onImageSelected(bitmap: Bitmap) {
        currentProfilePicture = bitmap
        getMvpView().setProfilePicture(bitmap)
    }
}