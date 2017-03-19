package backupstorage;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Abstraction layer on top of the Google drive storage. It uses the Google
 * Drive API Client Library for Java to connect to the Google drives rest API.
 * <br>
 * https://developers.google.com/api-client-library/java/apis/drive/v3
 * https://developers.google.com/drive/v3/web/quickstart/java
 */
public class GoogleDrive {

    private final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    private final HttpTransport httpTransport = GoogleNetHttpTransport
            .newTrustedTransport();
    private final Drive drive;

    private MediaHttpUploaderProgressListener uploadProgressListener;

    private MediaHttpDownloaderProgressListener downloadProgressListener;

    /**
     * To be able to use the API you have to register the application in the
     * Google Developers Console and download a file containing the secret used
     * to login.
     * <br>
     * https://developers.google.com/identity/protocols/OAuth2InstalledApp
     * https://developers.google.com/drive/v3/web/quickstart/java
     */
    public GoogleDrive(String applicationName, InputStream clientSecret,
            String credentialsDir) throws IOException, GeneralSecurityException {
        java.io.File credentialsStoreDirectory = new java.io.File(credentialsDir);
        FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(
                credentialsStoreDirectory);

        // If modifying these scopes, delete your previously saved credentials
        List<String> scopes = Arrays.asList(DriveScopes.DRIVE_FILE);

        Credential credential = authorize(clientSecret, scopes,
                dataStoreFactory);
        drive = new Drive.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(applicationName).build();
    }

    /**
     * Connect to Google drive using secret in a file.
     *
     * @see GoogleDrive#GoogleDrive(String, InputStream, String)
     */
    public GoogleDrive(String applicationName, java.io.File secret,
            String credentialsDir) throws IOException, GeneralSecurityException {
        this(applicationName, new FileInputStream(secret), credentialsDir);
    }

    /**
     * Connect to Google drive using secret in a string.
     *
     * @see GoogleDrive#GoogleDrive(String, InputStream, String)
     */
    public GoogleDrive(String applicationName, String secret,
            String credentialsDir) throws IOException, GeneralSecurityException {
        this(applicationName,
                new ByteArrayInputStream(secret.getBytes("UTF-8")),
                credentialsDir);
    }

    /**
     * Authorizes the installed application to access user's protected data.
     *
     * @param credentials
     *            OAuth credentials. Follow the instructions in:
     *            https://developers
     *            .google.com/drive/v3/web/quickstart/java#prerequisites to
     *            activate the API and download the OAuth credentials.
     * @param scopes
     *            Which permissions does the application need. For more
     *            information see:
     *            https://developers.google.com/drive/v3/web/about
     *            -auth#OAuth2Authorizing
     */
    private Credential authorize(InputStream credentials, List<String> scopes,
            FileDataStoreFactory dataStoreFactory) throws IOException {
        // load client secrets
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                jsonFactory, new InputStreamReader(credentials));

        // set up authorization code flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, scopes)
                .setDataStoreFactory(dataStoreFactory).setAccessType("offline")
                .build();

        // authorize
        return new AuthorizationCodeInstalledApp(flow,
                new LocalServerReceiver()).authorize("user");
    }

    /**
     * Optionally set a listener to monitor upload progress.
     */
    public void setUploadProgressListener(
            MediaHttpUploaderProgressListener uploadProgressListener) {
        this.uploadProgressListener = uploadProgressListener;
    }

    /**
     * Optionally set a listener to monitor download progress.
     */
    public void setDownloadProgressListener(
            MediaHttpDownloaderProgressListener downloadProgressListener) {
        this.downloadProgressListener = downloadProgressListener;
    }

    /**
     * Upload file with path uploadFilePath into folder with ID folderId.
     */
    public File uploadFile(String uploadFilePath, String folderId,
            String contentType, boolean useDirectUpload) throws IOException {
        return uploadFile(new java.io.File(uploadFilePath), folderId,
                contentType, useDirectUpload);
    }

    /**
     * Upload file uploadFile into folder with ID folderId.
     */
    public File uploadFile(java.io.File uploadFile, String folderId,
            String contentType, boolean useDirectUpload) throws IOException {
        return uploadStream(new BufferedInputStream(new FileInputStream(
                uploadFile)), uploadFile.length(), uploadFile.getName(),
                folderId, contentType, useDirectUpload);
    }

    /**
     * Upload "length" bytes from the input stream into a remote file called
     * fileName to be placed in the folder with ID folderId.
     */
    public File uploadStream(InputStream input, long length, String fileName,
            String folderId, String contentType, boolean useDirectUpload)
            throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        fileMetadata.setParents(Collections.singletonList(folderId));

        InputStreamContent mediaContent = new InputStreamContent(contentType,
                input);
        mediaContent.setLength(length);

        Drive.Files.Create create = drive.files().create(fileMetadata,
                mediaContent);
        MediaHttpUploader uploader = create.getMediaHttpUploader();
        uploader.setDirectUploadEnabled(useDirectUpload);
        if (uploadProgressListener != null) {
            uploader.setProgressListener(uploadProgressListener);
        }
        return create.execute();
    }

    /**
     * Download remote file uploadedFile into local folder with path
     * directoryForDownload.
     */
    public void downloadFile(File uploadedFile, String directoryForDownload,
            boolean useDirectDownload) throws IOException {
        java.io.File parentDir = new java.io.File(directoryForDownload);
        OutputStream out = new FileOutputStream(new java.io.File(parentDir,
                uploadedFile.getName()));

        downloadStream(uploadedFile.getId(), out, useDirectDownload);
    }

    /**
     * Download the content from a remote file with ID fileId into the output
     * stream "out".
     */
    public void downloadStream(String fileId, OutputStream out,
            boolean useDirectDownload) throws IOException {
        Drive.Files.Get request = drive.files().get(fileId);
        MediaHttpDownloader downloader = request.getMediaHttpDownloader();
        downloader.setDirectDownloadEnabled(useDirectDownload);
        if (downloadProgressListener != null) {
            downloader.setProgressListener(downloadProgressListener);
        }
        request.executeMediaAndDownloadTo(out);
    }

    /**
     * Create a remote folder with given name in the root directory.
     */
    public File createFolder(String name) throws IOException {
        /*
         * A folder is a file with the MIME type
         * application/vnd.google-apps.folder and with no extension.
         */
        File fileMetadata = new File();
        fileMetadata.setName(name);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        return drive.files().create(fileMetadata).setFields("id").execute();
    }

    /**
     * Create a remote folder with given name inside a folder with ID parentId.
     */
    public File createFolderIn(String name, String parentId) throws IOException {
        // TODO: reduce code duplication in this method
        File fileMetadata = new File();
        fileMetadata.setName(name);
        fileMetadata.setParents(Collections.singletonList(parentId));
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        return drive.files().create(fileMetadata).setFields("id").execute();
    }

    /**
     * Permanently delete a file, skipping the trash. Children are also deleted.
     *
     * @param fileId
     *            ID of the file to delete.
     */
    public void deleteFile(String fileId) throws IOException {
        drive.files().delete(fileId).execute();
    }

    /**
     * Return a list of remote file handlers referring to the files (or folders)
     * in the folder with given ID.
     */
    public List<File> searchInFolder(String id) throws IOException {
        return getFiles("'" + id + "' in parents");
    }

    /**
     * Return a list of remote file handlers referring to the files whose name
     * contain the given string.
     */
    public List<File> searchFilesByName(String name) throws IOException {
        return getFiles("mimeType != 'application/vnd.google-apps.folder' and name contains '"
                + name + "'");
    }

    /**
     * Return a list of remote file handlers referring to the folders whose name
     * match exactly the given string.
     */
    public List<File> searchFolderByName(String name) throws IOException {
        return getFiles("mimeType = 'application/vnd.google-apps.folder' and name = '"
                + name + "'");
    }

    /**
     * Return a list of remote file handlers referring to the folders whose name
     * match exactly the given string and whose parent folder has ID
     * parentFolderId.
     */
    public List<File> searchFolderByNameIn(String name, String parentFolderId)
            throws IOException {
        return getFiles("mimeType = 'application/vnd.google-apps.folder' and name = '"
                + name + "' and '" + parentFolderId + "' in parents");
    }

    /**
     * Return a list of remote file handlers using a general query.
     * <br>
     * https://developers.google.com/drive/v3/web/search-parameters
     */
    protected List<File> getFiles(String query) throws IOException {
        List<File> files = new ArrayList<File>();
        String pageToken = null;
        do {
            FileList result = drive.files().list().setQ(query)
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken).execute();
            files.addAll(result.getFiles());
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        return files;
    }

    /**
     * Get remote file handler for file with ID fileId.
     */
    public File getFile(String fileId) throws IOException {
        return drive.files().get(fileId).execute();
    }
}
