/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.liuqi.ui.common.service.def;

import com.liuqi.ui.common.entity.Entity;
import com.liuqi.ui.common.service.EntityService;
import com.liuqi.util.ObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 文件服务抽象类
 *
 * @author LiuQI 2018/7/11 8:52
 * @version V1.0
 **/
public class FileEntityService implements EntityService {
    private Path filePath;
    private List<Entity> entityList = new ArrayList<>();

    private static final Logger logger = LoggerFactory.getLogger(FileEntityService.class);

    public FileEntityService(String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }

        filePath = Paths.get(file.toURI());

        readFromFile();

        entityList.forEach(entity -> {
            String id = entity.getOrDefault("id", "").toString();
            if ("".equals(id)) {
                entity.put("id", UUID.randomUUID().toString());
            }

            List<Entity> children = (List<Entity>) entity.get("children");
            if (null != children) {
                children.forEach(child -> {
                    String childId = child.getOrDefault("id", "").toString();
                    if ("".equals(childId)) {
                        child.put("id", UUID.randomUUID().toString());
                    }
                });
            }
        });

        saveToFile();
    }

    @Override
    public void save(Entity entity) {
        String id = entity.getOrDefault("id", "").toString();
        String parentId = entity.getOrDefault("parentId", "").toString();
        if ("".equals(id)) {
            entity.put("id", UUID.randomUUID().toString());
            entity.put("createTime", new Date());

            entity = ObjectUtil.copy(entity);

            if (!"".equals(parentId)) {
                Entity parentEntity = getEntity(parentId);

                if (null != parentEntity) {
                    List<Entity> children = (List<Entity>) parentEntity.computeIfAbsent("children", a -> new ArrayList<>(16));
                    children.add(entity);
                    save(parentEntity);
                }
            } else {
                entityList.add(entity);
            }
        } else {
            List<Entity> entityList;

            if ("".equals(parentId)) {
                entityList = this.entityList;
            } else {
                Entity parentEntity = getEntity(parentId);

                if (null != parentEntity) {
                    entityList = (List<Entity>) parentEntity.computeIfAbsent("children", a -> new ArrayList<>(16));
                } else {
                    entityList = this.entityList;
                }
            }

            if (null != entityList) {
                for (int i = 0, size = entityList.size(); i < size; i++) {
                    Entity entity1 = entityList.get(i);
                    if (entity1.get("id").equals(id)) {
                        entityList.set(i, ObjectUtil.copy(entity));
                        break;
                    }
                }
            }
        }

        saveToFile();
    }

    private Entity getEntity(String id) {
        Entity parentEntity = null;
        for (Entity pEntity : entityList) {
            if (id.equals(pEntity.getOrDefault("id", ""))) {
                parentEntity = pEntity;
                break;
            }
        }
        return parentEntity;
    }

    @Override
    public List<Entity> list() {
        if (null == entityList) {
            entityList = new ArrayList<>();
        }

        return ObjectUtil.copy(entityList);
    }

    private void saveToFile() {
        if (null != entityList) {
            OutputStream writer = null;
            try {
                writer = Files.newOutputStream(filePath);
            } catch (IOException e) {

                return;
            }
            ObjectOutputStream outputStream = null;
            try {
                outputStream = new ObjectOutputStream(writer);
                outputStream.writeObject(entityList);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (null != outputStream) {
                    try {
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void readFromFile() {
        InputStream inputStream = null;
        try {
            inputStream = Files.newInputStream(filePath);
        } catch (IOException e) {
            logger.error("读取文件失败，文件：" + filePath, e);
            return ;
        }
        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(inputStream);
        } catch (IOException e) {
            logger.error("读取文件失败，文件：" + filePath, e);
            return;
        }
        try {
            Object obj = objectInputStream.readObject();
            if (obj instanceof List) {
                entityList = (List<Entity>) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                objectInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
