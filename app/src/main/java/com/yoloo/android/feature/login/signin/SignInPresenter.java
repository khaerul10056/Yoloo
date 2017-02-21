package com.yoloo.android.feature.login.signin;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.yoloo.android.data.faker.AccountFaker;
import com.yoloo.android.data.repository.user.UserRepository;
import com.yoloo.android.feature.login.AuthUI;
import com.yoloo.android.feature.login.FacebookProvider;
import com.yoloo.android.feature.login.GoogleProvider;
import com.yoloo.android.feature.login.IdpResponse;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

class SignInPresenter extends MvpPresenter<SignInView> {

  private final UserRepository userRepository;

  SignInPresenter(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override public void onDetachView() {
    getView().onHideLoading();
    super.onDetachView();
  }

  void signIn(IdpResponse response) {
    getView().onShowLoading();

    final String providerType = response.getProviderType();
    if (providerType.equals(AuthUI.GOOGLE_PROVIDER)) {
      processFirebase(GoogleProvider.createAuthCredential(response));
    } else if (providerType.equals(AuthUI.FACEBOOK_PROVIDER)) {
      processFirebase(FacebookProvider.createAuthCredential(response));
    }
  }

  void signIn(String email, String password) {
    if (email.contains("berna")) {
      AccountFaker.setGirlUser();
    }
    if (email.contains("yunus")) {
      AccountFaker.setMaleUser();
    }

    getView().onHideLoading();
    getView().onSignedIn();
    //processFirebase(EmailAuthProvider.getCredential(email, password));
  }

  private void processFirebase(AuthCredential credential) {
    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(task -> {
      Timber.d("signInWithCredential:onComplete %s", task.isSuccessful());

      // If sign in fails, display a message to the user. If sign in succeeds
      // the auth state listener will be notified and logic to handle the
      // signed in user can be handled in the listener.
      if (!task.isSuccessful()) {
        getView().onHideLoading();
        getView().onError(task.getException());
      } else {
        loadUser();
      }
    });
  }

  private void loadUser() {
    Disposable d = userRepository.getMe()
        .observeOn(AndroidSchedulers.mainThread(), true)
        .subscribe(account -> {
          getView().onHideLoading();
          getView().onSignedIn();
        }, throwable -> {
          getView().onHideLoading();
          getView().onError(throwable);
        });

    getDisposable().add(d);
  }
}
