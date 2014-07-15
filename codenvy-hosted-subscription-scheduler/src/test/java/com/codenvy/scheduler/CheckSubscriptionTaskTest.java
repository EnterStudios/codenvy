/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.scheduler;

import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.shared.dto.Subscription;
import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.ConflictException;
import com.codenvy.api.core.ForbiddenException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.dto.server.DtoFactory;

import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.codenvy.scheduler.SubscriptionScheduler.CheckState.ABORT_CHECK;
import static com.codenvy.scheduler.SubscriptionScheduler.CheckState.CONTINUE_CHECK;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SubscriptionScheduler}
 *
 * @author Alexander Garagatyi
 */
@Listeners(value = {MockitoTestNGListener.class})
public class CheckSubscriptionTaskTest {
    @Mock
    private AccountDao accountDao;

    @Mock
    private SubscriptionSchedulerHandler handler1;

    @Mock
    private SubscriptionSchedulerHandler handler2;

    private Subscription subscription1;

    private Subscription subscription2;

    private SubscriptionScheduler subscriptionScheduler;

    private SubscriptionScheduler.CheckSubscriptionsTask task;

    private boolean continueTest;

    @BeforeMethod
    public void setUp() throws Exception {
        Set<SubscriptionSchedulerHandler> handlers = new HashSet<>(Arrays.asList(handler1, handler2));
        // should be used first
        when(handler1.getPriority()).thenReturn(0);
        when(handler2.getPriority()).thenReturn(1);

        subscriptionScheduler = new SubscriptionScheduler(handlers, accountDao);

        task = subscriptionScheduler.new CheckSubscriptionsTask();

        continueTest = false;

        subscription1 = DtoFactory.getInstance().createDto(Subscription.class).withId("id1");
        subscription2 = DtoFactory.getInstance().createDto(Subscription.class).withId("id2");
    }

    @Test
    public void shouldBeAbleToCallAllHandlers() throws ApiException {
        when(accountDao.getAllSubscriptions()).thenReturn(Arrays.asList(subscription1, subscription2).iterator());
        when(handler1.checkSubscription(any(Subscription.class))).thenReturn(CONTINUE_CHECK);
        when(handler2.checkSubscription(any(Subscription.class))).thenReturn(CONTINUE_CHECK);

        task.run();

        verify(handler1).checkSubscription(subscription1);
        verify(handler2).checkSubscription(subscription1);
        verify(handler1).checkSubscription(subscription2);
        verify(handler2).checkSubscription(subscription2);
    }

    @Test
    public void shouldNotCallHandlersIfExceptionOccursOnGetAllSubscriptions() throws ApiException {
        when(accountDao.getAllSubscriptions()).thenThrow(new ServerException(""));
        when(handler1.checkSubscription(any(Subscription.class))).thenReturn(CONTINUE_CHECK);
        when(handler2.checkSubscription(any(Subscription.class))).thenReturn(CONTINUE_CHECK);

        task.run();

        verify(handler1, never()).checkSubscription(any(Subscription.class));
        verify(handler2, never()).checkSubscription(any(Subscription.class));
    }

    @Test
    public void shouldNotCallHandlersIfThereIsNoSubscriptionsOnGetAllSubscriptions() throws ApiException {
        when(accountDao.getAllSubscriptions()).thenReturn(Collections.<Subscription>emptyIterator());
        when(handler1.checkSubscription(any(Subscription.class))).thenReturn(CONTINUE_CHECK);
        when(handler2.checkSubscription(any(Subscription.class))).thenReturn(CONTINUE_CHECK);

        task.run();

        verify(handler1, never()).checkSubscription(any(Subscription.class));
        verify(handler2, never()).checkSubscription(any(Subscription.class));
    }

    @Test
    public void shouldNotCallHandlersIfThreadIsInterruptedBeforeChecksOnGetAllSubscriptions() throws ApiException, InterruptedException {
        when(accountDao.getAllSubscriptions()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Thread.currentThread().interrupt();
                continueTest = true;
                return Arrays.asList(subscription1, subscription2).iterator();
            }
        });
        when(handler1.checkSubscription(any(Subscription.class))).thenReturn(CONTINUE_CHECK);
        when(handler2.checkSubscription(any(Subscription.class))).thenReturn(CONTINUE_CHECK);

        task = subscriptionScheduler.new CheckSubscriptionsTask();
        Thread separateThread = new Thread(task);
        separateThread.start();
        while (!continueTest) {
            Thread.sleep(100);
        }

        verify(handler1, never()).checkSubscription(any(Subscription.class));
        verify(handler2, never()).checkSubscription(any(Subscription.class));
    }

    @Test
    public void shouldNotCallHandlersForSecondSubscriptionIfThreadIsInterruptedOnCheckFirstSubscription()
            throws ApiException, InterruptedException {
        when(accountDao.getAllSubscriptions()).thenReturn(Arrays.asList(subscription1, subscription2).iterator());
        when(handler1.checkSubscription(eq(subscription1))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Thread.currentThread().interrupt();
                continueTest = true;
                return CONTINUE_CHECK;
            }
        });
        when(handler2.checkSubscription(eq(subscription1))).thenReturn(CONTINUE_CHECK);
        when(handler1.checkSubscription(eq(subscription2))).thenReturn(CONTINUE_CHECK);
        when(handler2.checkSubscription(eq(subscription2))).thenReturn(CONTINUE_CHECK);

        task = subscriptionScheduler.new CheckSubscriptionsTask();
        Thread separateThread = new Thread(task);
        separateThread.start();
        while (!continueTest) {
            Thread.sleep(100);
        }

        verify(handler1).checkSubscription(eq(subscription1));
        verify(handler2).checkSubscription(eq(subscription1));
        verify(handler1, never()).checkSubscription(eq(subscription2));
        verify(handler2, never()).checkSubscription(eq(subscription2));
    }

    @Test(dataProvider = "apiExceptionProvider")
    public void shouldContinueChecksIfApiExceptionThrownOnCheck(ApiException e) throws ApiException {
        when(accountDao.getAllSubscriptions()).thenReturn(Arrays.asList(subscription1, subscription2).iterator());
        when(handler1.checkSubscription(any(Subscription.class))).thenThrow(e);
        when(handler2.checkSubscription(any(Subscription.class))).thenReturn(CONTINUE_CHECK);

        task.run();

        verify(handler1).checkSubscription(subscription1);
        verify(handler2).checkSubscription(subscription1);
        verify(handler1).checkSubscription(subscription2);
        verify(handler2).checkSubscription(subscription2);
    }

    @Test
    public void shouldNotCheckWithNextHandlerIfHandlerReturnAbort() throws ApiException {
        when(accountDao.getAllSubscriptions()).thenReturn(Arrays.asList(subscription1, subscription2).iterator());
        when(handler1.checkSubscription(eq(subscription1))).thenReturn(ABORT_CHECK);
        when(handler1.checkSubscription(eq(subscription2))).thenReturn(CONTINUE_CHECK);
        when(handler2.checkSubscription(any(Subscription.class))).thenReturn(CONTINUE_CHECK);

        task.run();

        verify(handler1).checkSubscription(subscription1);
        verify(handler2, never()).checkSubscription(subscription1);
        verify(handler1).checkSubscription(subscription2);
        verify(handler2).checkSubscription(subscription2);
    }

    @DataProvider(name = "apiExceptionProvider")
    public ApiException[][] apiExceptionProvider() {
        return new ApiException[][]{{new NotFoundException("")},
                                    {new ServerException("")},
                                    {new ForbiddenException("")},
                                    {new ConflictException("")}
        };
    }
}