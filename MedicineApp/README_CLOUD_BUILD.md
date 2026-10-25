# Build Medicine Reminder APK in GitHub Actions (no Android Studio required)

This project uses Java/XML, Android Gradle Plugin 8.2.2, compile SDK 34, and Java 11 source compatibility. The workflow uses JDK 17 and Gradle 8.2 in GitHub's cloud runner.

## Steps
1. Create or open a GitHub repository.
2. Upload the **contents** of this `MedicineApp` folder to the repository root. Keep `.github/workflows/build-apk.yml` in that exact path.
3. Commit the files to the default branch.
4. Open the repository's **Actions** tab.
5. Select **Build Android APK** on the left, then choose **Run workflow** and confirm.
6. Wait for the run to finish. If successful, open the run and find the **Artifacts** section.
7. Download `MedicineReminder-debug-apk.zip` and extract `app-debug.apk`.
8. Transfer `app-debug.apk` to your Android phone and install it. Android may ask you to allow installation from that source.

## Notes
- This workflow creates a debug APK for testing and direct installation, not a Play Store release.
- The APK has not been built or tested until the GitHub Actions workflow completes successfully.
- If the build fails, open the workflow run, copy the first relevant error from the build log, and use it to troubleshoot.
- The source tree includes `gradle-wrapper.properties` but may not include the wrapper JAR/scripts; this workflow invokes Gradle installed by the setup action, so the wrapper is not required for this cloud build.
