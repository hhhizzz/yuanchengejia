package com.xunix.ycej.message;


import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.ProgressCallback;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.im.v2.AVIMMessageCreator;
import com.avos.avoscloud.im.v2.AVIMMessageField;
import com.avos.avoscloud.im.v2.AVIMMessageType;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.messages.AVIMFileMessage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xunixhuang on 6/9/16.
 */
@AVIMMessageType(type = 1)
public class AVIMPDFMessage extends AVIMTypedMessage {
    static final String OBJECT_ID = "objId";
    static final String FILE_URL = "url";
    static final String FILE_META = "metaData";
    static final String FILE_SIZE = "size";
    static final String FORMAT = "format";
    static final String DURATION = "duration";
    private static final String LOCAL_PATH = "local_path";
    File localFile;
    AVFile actualFile;
    @AVIMMessageField(
            name = "_lcfile"
    )
    protected Map<String, Object> file;
    @AVIMMessageField(
            name = "_lctext"
    )
    String text;
    @AVIMMessageField(
            name = "_lcattrs"
    )
    Map<String, Object> attrs;
    ProgressCallback progressCallback;
    public static final AVIMMessageCreator<AVIMFileMessage> CREATOR = new AVIMMessageCreator(AVIMFileMessage.class);

    public AVIMPDFMessage() {
    }

    public AVIMPDFMessage(String localPath) throws IOException {
        this(new File(localPath));
    }

    public AVIMPDFMessage(File localFile) throws IOException {
        this.localFile = localFile;
        this.actualFile = AVFile.withFile(localFile.getName(), localFile);
        this.file = new HashMap();
        this.file.put("local_path", localFile.getPath());
    }

    public AVIMPDFMessage(AVFile file) {
        this.actualFile = file;
    }

    public Map<String, Object> getFile() {
        return this.file;
    }

    public String getLocalFilePath() {
        return null != this.localFile && this.localFile.exists()?this.localFile.getPath():null;
    }

    public AVFile getAVFile() {
        if(this.actualFile != null) {
            return this.actualFile;
        } else if(this.file.containsKey("url")) {
            Map avfileMeta = null;
            if(this.file.containsKey("metaData")) {
                avfileMeta = (Map)this.file.get("metaData");
            }

            AVFile avfile = new AVFile((String)null, (String)this.file.get("url"), avfileMeta);
            if(this.file.containsKey("objId")) {
                avfile.setObjectId((String)this.file.get("objId"));
            }

            return avfile;
        } else {
            return null;
        }
    }

    protected void setFile(Map<String, Object> file) {
        this.file = file;
        Map metaData = (Map)file.get("metaData");
        this.actualFile = new AVFile((String)null, (String)file.get("url"), metaData);
        this.actualFile.setObjectId((String)file.get("objId"));
        if(file.containsKey("local_path")) {
            this.localFile = new File((String)file.get("local_path"));
        }

    }

    public String getFileUrl() {
        return this.file != null?(String)this.file.get("url"):null;
    }
    public String getFileName() {
        return this.file != null?(String)this.file.get("name"):null;
    }
    public void setFileUrl(String url){
        file.put("url",url);
    }
    public void setFileName(String name){
        file.put("name",name);
    }


    public Map<String, Object> getFileMetaData() {
        if(this.file == null) {
            this.file = new HashMap();
        }

        Object meta;
        if(!this.file.containsKey("metaData")) {
            meta = new HashMap();
            ((Map)meta).put("size", Integer.valueOf(this.actualFile.getSize()));
        } else {
            meta = (Map)this.file.get("metaData");
        }

        return (Map)meta;
    }

    public long getSize() {
        Map meta = this.getFileMetaData();
        return meta != null && meta.containsKey("size")? Long.valueOf(meta.get("size").toString()).longValue():0L;
    }

    protected void upload(final SaveCallback callback) {
        if(this.actualFile != null) {
            this.actualFile.saveInBackground(new SaveCallback() {
                public void done(AVException e) {
                    if(e != null) {
                        callback.internalDone(e);
                    } else {
                        AVIMPDFMessage.this.fulFillFileInfo(callback);
                    }

                }
            }, this.progressCallback);
        } else {
            callback.internalDone(new AVException(new RuntimeException("cannot find the file!")));
        }

    }

    public void setProgressCallback(ProgressCallback callback) {
        this.progressCallback = callback;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Map<String, Object> getAttrs() {
        return this.attrs;
    }

    public void setAttrs(Map<String, Object> attr) {
        this.attrs = attr;
    }

    protected void fulFillFileInfo(final SaveCallback callback) {
        if(this.actualFile != null) {
            this.file = (Map)(this.getFile() == null?new HashMap():this.getFile());
            this.file.put("objId", this.actualFile.getObjectId());
            this.file.put("url", this.actualFile.getUrl());
            this.file.remove("local_path");
            final Object metaData = this.getFileMetaData() == null?new HashMap():this.getFileMetaData();
            if(!((Map)metaData).containsKey("size")) {
                ((Map)metaData).put("size", Integer.valueOf(this.actualFile.getSize()));
            }

            if(null != this.actualFile.getMetaData() && this.actualFile.getMetaData().containsKey("__source") && this.actualFile.getMetaData().get("__source").equals("external")) {
                callback.internalDone((AVException)null);
            } else {
                this.getAdditionalMetaData((Map)metaData, new SaveCallback() {
                    public void done(AVException e) {
                        AVIMPDFMessage.this.file.put("metaData", metaData);
                        if(callback != null) {
                            callback.internalDone(e);
                        }

                    }
                });
            }
        } else {
            callback.internalDone(new AVException(new RuntimeException("cannot find the file!")));
        }

    }

    protected void getAdditionalMetaData(Map<String, Object> meta, SaveCallback callback) {
        callback.internalDone((AVException)null);
    }
}
