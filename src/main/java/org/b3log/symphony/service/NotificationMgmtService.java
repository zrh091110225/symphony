/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.symphony.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.CompositeFilterOperator;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.annotation.Transactional;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.symphony.model.Notification;
import org.b3log.symphony.repository.NotificationRepository;
import org.json.JSONObject;

/**
 * Notification management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.7.0.3, Aug 31, 2016
 * @since 0.2.5
 */
@Service
public class NotificationMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(NotificationMgmtService.class.getName());

    /**
     * Notification repository.
     */
    @Inject
    private NotificationRepository notificationRepository;

    /**
     * Adds a 'invitecode used' type notification with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "userId"; "",
     *     "dataId": "" // invited user id
     * }
     * </pre>
     *
     * @throws ServiceException service exception
     */
    @Transactional
    public void addInvitecodeUsedNotification(final JSONObject requestJSONObject) throws ServiceException {
        try {
            requestJSONObject.put(Notification.NOTIFICATION_DATA_TYPE, Notification.DATA_TYPE_C_INVITECODE_USED);

            addNotification(requestJSONObject);
        } catch (final RepositoryException e) {
            final String msg = "Adds notification [type=invitecode_used] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * Makes the specified user's notifications of the specified type as read.
     *
     * @param userId the specified user id
     * @param type the specified notification type
     */
    public void makeRead(final String userId, final int type) {
        final Query query = new Query().setFilter(
                CompositeFilterOperator.and(
                        new PropertyFilter(Notification.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId),
                        new PropertyFilter(Notification.NOTIFICATION_HAS_READ, FilterOperator.EQUAL, false),
                        new PropertyFilter(Notification.NOTIFICATION_DATA_TYPE, FilterOperator.EQUAL, type)));

        try {
            final Set<JSONObject> notifications = CollectionUtils.jsonArrayToSet(notificationRepository.get(query).
                    optJSONArray(Keys.RESULTS));

            makeRead(notifications);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Makes read failed", e);
        }
    }

    /**
     * Makes the specified user to article, comments notifications as read.
     *
     * @param userId the specified user id
     * @param articleId the specified article id
     * @param commentIds the specified comment ids
     */
    public void makeRead(final String userId, final String articleId, final List<String> commentIds) {
        final Set<String> dataIds = new HashSet<String>(commentIds);
        dataIds.add(articleId);

        final Query query = new Query().setFilter(
                CompositeFilterOperator.and(
                        new PropertyFilter(Notification.NOTIFICATION_USER_ID, FilterOperator.EQUAL, userId),
                        new PropertyFilter(Notification.NOTIFICATION_HAS_READ, FilterOperator.EQUAL, false),
                        new PropertyFilter(Notification.NOTIFICATION_DATA_ID, FilterOperator.IN, dataIds)));

        try {
            final Set<JSONObject> notifications = CollectionUtils.jsonArrayToSet(notificationRepository.get(query).
                    optJSONArray(Keys.RESULTS));

            makeRead(notifications);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Makes read failed", e);
        }
    }

    /**
     * Makes the specified notifications have been read.
     *
     * @param notifications the specified notifications
     * @throws ServiceException service exception
     */
    @Transactional
    public void makeRead(final Collection<JSONObject> notifications) throws ServiceException {
        for (final JSONObject notification : notifications) {
            makeRead(notification);
        }
    }

    /**
     * Makes the specified notification have been read.
     *
     * @param notification the specified notification, return directly if this notification has been read
     * (notification.hasRead equals to {@code true})
     * @throws ServiceException service exception
     */
    @Transactional
    public void makeRead(final JSONObject notification) throws ServiceException {
        if (notification.optBoolean(Notification.NOTIFICATION_HAS_READ)) {
            return;
        }

        final String id = notification.optString(Keys.OBJECT_ID);

        try {
            final JSONObject record = notificationRepository.get(id);

            record.put(Notification.NOTIFICATION_HAS_READ, true);

            notificationRepository.update(id, record);
        } catch (final RepositoryException e) {
            final String msg = "Makes notification as read failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * Adds a 'broadcast' type notification with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "userId"; "",
     *     "dataId": "" // article id
     * }
     * </pre>
     *
     * @throws ServiceException service exception
     */
    @Transactional
    public void addBroadcastNotification(final JSONObject requestJSONObject) throws ServiceException {
        try {
            requestJSONObject.put(Notification.NOTIFICATION_DATA_TYPE, Notification.DATA_TYPE_C_BROADCAST);

            addNotification(requestJSONObject);
        } catch (final RepositoryException e) {
            final String msg = "Adds notification [type=broadcast] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * Adds a 'point charge' type notification with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "userId"; "",
     *     "dataId": "" // transfer record id
     * }
     * </pre>
     *
     * @throws ServiceException service exception
     */
    @Transactional
    public void addPointChargeNotification(final JSONObject requestJSONObject) throws ServiceException {
        try {
            requestJSONObject.put(Notification.NOTIFICATION_DATA_TYPE, Notification.DATA_TYPE_C_POINT_CHARGE);

            addNotification(requestJSONObject);
        } catch (final RepositoryException e) {
            final String msg = "Adds notification [type=point_charge] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * Adds a 'abuse point deduct' type notification with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "userId"; "",
     *     "dataId": "" // transfer record id
     * }
     * </pre>
     *
     * @throws ServiceException service exception
     */
    @Transactional
    public void addAbusePointDeductNotification(final JSONObject requestJSONObject) throws ServiceException {
        try {
            requestJSONObject.put(Notification.NOTIFICATION_DATA_TYPE, Notification.DATA_TYPE_C_ABUSE_POINT_DEDUCT);

            addNotification(requestJSONObject);
        } catch (final RepositoryException e) {
            final String msg = "Adds notification [type=abuse_point_deduct] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * Adds a 'point exchange' type notification with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "userId"; "",
     *     "dataId": "" // transfer record id
     * }
     * </pre>
     *
     * @throws ServiceException service exception
     */
    @Transactional
    public void addPointExchangeNotification(final JSONObject requestJSONObject) throws ServiceException {
        try {
            requestJSONObject.put(Notification.NOTIFICATION_DATA_TYPE, Notification.DATA_TYPE_C_POINT_EXCHANGE);

            addNotification(requestJSONObject);
        } catch (final RepositoryException e) {
            final String msg = "Adds notification [type=point_exchange] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * Adds a 'point transfer' type notification with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "userId"; "",
     *     "dataId": "" // transfer record id
     * }
     * </pre>
     *
     * @throws ServiceException service exception
     */
    @Transactional
    public void addPointTransferNotification(final JSONObject requestJSONObject) throws ServiceException {
        try {
            requestJSONObject.put(Notification.NOTIFICATION_DATA_TYPE, Notification.DATA_TYPE_C_POINT_TRANSFER);

            addNotification(requestJSONObject);
        } catch (final RepositoryException e) {
            final String msg = "Adds notification [type=point_transfer] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * Adds a 'article reward' type notification with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "userId"; "",
     *     "dataId": "" // reward id
     * }
     * </pre>
     *
     * @throws ServiceException service exception
     */
    @Transactional
    public void addArticleRewardNotification(final JSONObject requestJSONObject) throws ServiceException {
        try {
            requestJSONObject.put(Notification.NOTIFICATION_DATA_TYPE, Notification.DATA_TYPE_C_POINT_ARTICLE_REWARD);

            addNotification(requestJSONObject);
        } catch (final RepositoryException e) {
            final String msg = "Adds notification [type=article_reward] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * Adds a 'article thank' type notification with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "userId"; "",
     *     "dataId": "" // thank id
     * }
     * </pre>
     *
     * @throws ServiceException service exception
     */
    @Transactional
    public void addArticleThankNotification(final JSONObject requestJSONObject) throws ServiceException {
        try {
            requestJSONObject.put(Notification.NOTIFICATION_DATA_TYPE, Notification.DATA_TYPE_C_POINT_ARTICLE_THANK);

            addNotification(requestJSONObject);
        } catch (final RepositoryException e) {
            final String msg = "Adds notification [type=article_thank] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * Adds a 'comment thank' type notification with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "userId"; "",
     *     "dataId": "" // reward id
     * }
     * </pre>
     *
     * @throws ServiceException service exception
     */
    @Transactional
    public void addCommentThankNotification(final JSONObject requestJSONObject) throws ServiceException {
        try {
            requestJSONObject.put(Notification.NOTIFICATION_DATA_TYPE, Notification.DATA_TYPE_C_POINT_COMMENT_THANK);

            addNotification(requestJSONObject);
        } catch (final RepositoryException e) {
            final String msg = "Adds notification [type=comment_thank] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * Adds a 'comment' type notification with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "userId"; "",
     *     "dataId": ""
     * }
     * </pre>
     *
     * @throws ServiceException service exception
     */
    // XXX: Unused
    @Transactional
    public void addCommentNotification(final JSONObject requestJSONObject) throws ServiceException {
        try {
            requestJSONObject.put(Notification.NOTIFICATION_DATA_TYPE, Notification.DATA_TYPE_C_COMMENT);

            addNotification(requestJSONObject);
        } catch (final RepositoryException e) {
            final String msg = "Adds notification [type=comment] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * Adds a 'at' type notification with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "userId"; "",
     *     "dataId": ""
     * }
     * </pre>
     *
     * @throws ServiceException service exception
     */
    @Transactional
    public void addAtNotification(final JSONObject requestJSONObject) throws ServiceException {
        try {
            requestJSONObject.put(Notification.NOTIFICATION_DATA_TYPE, Notification.DATA_TYPE_C_AT);

            addNotification(requestJSONObject);
        } catch (final RepositoryException e) {
            final String msg = "Adds notification [type=at] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * Adds a 'article' type notification with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "userId"; "",
     *     "dataId": ""
     * }
     * </pre>
     *
     * @throws ServiceException service exception
     */
    // XXX: Unused
    @Transactional
    public void addArticleNotification(final JSONObject requestJSONObject) throws ServiceException {
        try {
            requestJSONObject.put(Notification.NOTIFICATION_DATA_TYPE, Notification.DATA_TYPE_C_ARTICLE);

            addNotification(requestJSONObject);
        } catch (final RepositoryException e) {
            final String msg = "Adds notification [type=article] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * Adds a 'followingUser' type notification with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "userId"; "",
     *     "dataId": "" // article id
     * }
     * </pre>
     *
     * @throws ServiceException service exception
     */
    @Transactional
    public void addFollowingUserNotification(final JSONObject requestJSONObject) throws ServiceException {
        try {
            requestJSONObject.put(Notification.NOTIFICATION_DATA_TYPE, Notification.DATA_TYPE_C_FOLLOWING_USER);

            addNotification(requestJSONObject);
        } catch (final RepositoryException e) {
            final String msg = "Adds notification [type=followingUser] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * Adds a 'commented' type notification with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "userId"; "",
     *     "dataId": ""
     * }
     * </pre>
     *
     * @throws ServiceException service exception
     */
    @Transactional
    public void addCommentedNotification(final JSONObject requestJSONObject) throws ServiceException {
        try {
            requestJSONObject.put(Notification.NOTIFICATION_DATA_TYPE, Notification.DATA_TYPE_C_COMMENTED);

            addNotification(requestJSONObject);
        } catch (final RepositoryException e) {
            final String msg = "Adds notification [type=commented] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * Adds a 'reply' type notification with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "userId"; "",
     *     "dataId": ""
     * }
     * </pre>
     *
     * @throws ServiceException service exception
     */
    @Transactional
    public void addReplyNotification(final JSONObject requestJSONObject) throws ServiceException {
        try {
            requestJSONObject.put(Notification.NOTIFICATION_DATA_TYPE, Notification.DATA_TYPE_C_REPLY);

            addNotification(requestJSONObject);
        } catch (final RepositoryException e) {
            final String msg = "Adds notification [type=reply] failed";
            LOGGER.log(Level.ERROR, msg, e);

            throw new ServiceException(msg);
        }
    }

    /**
     * Adds a notification with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     * {
     *     "userId"; "",
     *     "dataId": "",
     *     "dataType": int
     * }
     * </pre>
     *
     * @throws RepositoryException repository exception
     */
    private void addNotification(final JSONObject requestJSONObject) throws RepositoryException {
        final JSONObject notification = new JSONObject();

        notification.put(Notification.NOTIFICATION_HAS_READ, false);
        notification.put(Notification.NOTIFICATION_USER_ID, requestJSONObject.optString(Notification.NOTIFICATION_USER_ID));
        notification.put(Notification.NOTIFICATION_DATA_ID, requestJSONObject.optString(Notification.NOTIFICATION_DATA_ID));
        notification.put(Notification.NOTIFICATION_DATA_TYPE, requestJSONObject.optInt(Notification.NOTIFICATION_DATA_TYPE));

        notificationRepository.add(notification);
    }
}
