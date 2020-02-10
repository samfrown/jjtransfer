package io.github.samfrown.moneytransfer.service;

import io.github.samfrown.moneytransfer.dao.TransfersDao;
import io.github.samfrown.moneytransfer.domain.Transfer;
import io.github.samfrown.moneytransfer.rest.request.TransferFromAccountRequest;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.javamoney.moneta.FastMoney;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;

import java.util.Queue;
import java.util.UUID;

import static io.github.samfrown.moneytransfer.domain.Account.DEFAULT_CURRENCY;
import static io.github.samfrown.moneytransfer.domain.Transfer.State.COMPLETED;
import static io.github.samfrown.moneytransfer.domain.Transfer.State.PROCESSING;
import static io.github.samfrown.moneytransfer.domain.Transfer.State.REJECTED;
import static io.github.samfrown.moneytransfer.domain.Transfer.State.REJECTING;
import static junitparams.JUnitParamsRunner.$;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(JUnitParamsRunner.class)
public class TransferServiceTest {

    private static final String TEST_SOURCE_ACCOUNT_ID = "Alice";
    private static final String TEST_DEST_ACCOUNT_ID = "Bob";
    private static final Number TEST_AMOUNT = 13.79;

    //target
    private TransferService transferService;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private TransfersDao transfersDaoMock;

    @Mock
    private Queue<UUID> transferChannelMock;

    private final TransferFromAccountRequest transferRequest = new TransferFromAccountRequest(TEST_DEST_ACCOUNT_ID, TEST_AMOUNT);
    private final Transfer.Builder transferBuilder = Transfer.builder().sourceAccountId(TEST_SOURCE_ACCOUNT_ID)
            .destinationAccountId(TEST_DEST_ACCOUNT_ID)
            .transferAmount(FastMoney.of(TEST_AMOUNT, DEFAULT_CURRENCY))
            .state(Transfer.State.NEW)
            .transferId(UUID.randomUUID());

    @Before
    public void setUp() throws Exception {
        transferService = new TransferService(transfersDaoMock, transferChannelMock);
    }

    @Test
    public void shouldCreateTransferForAnotherAccount() {
        //when
        Transfer actualTransfer = transferService.create(TEST_SOURCE_ACCOUNT_ID, transferRequest);
        //then
        Transfer expectedTransfer = transferBuilder.transferId(actualTransfer.getTransferId()).build();
        assertThat(actualTransfer, is(expectedTransfer));
        verifyNoInteractions(transfersDaoMock);
    }

    @Test
    public void shouldUpdateTransferOnPrepare() {
        //given
        UUID originalUuid = UUID.randomUUID();
        transferRequest.setTransferAmount(123);
        transferRequest.setDestinationAccountId("Fred");
        Transfer expectedTransfer = transferBuilder.transferId(originalUuid)
                .destinationAccountId(transferRequest.getDestinationAccountId())
                .transferAmount(FastMoney.of(123, DEFAULT_CURRENCY)).build();
        //when
        Transfer actualTransfer = transferService.prepare(originalUuid, TEST_SOURCE_ACCOUNT_ID, transferRequest);
        //then
        assertThat(actualTransfer, is(expectedTransfer));
        verifyNoInteractions(transfersDaoMock);
        verifyNoInteractions(transferChannelMock);
    }

    @Test
    public void shouldProcessingTransferOnCommit() {
        //given
        Transfer preparedTransfer = transferBuilder.transferId(UUID.randomUUID()).build();
        Transfer expectedTransfer = preparedTransfer.toBuilder().state(PROCESSING).build();

        //when
        Transfer committedTransfer = transferService.commit(preparedTransfer);
        //then
        assertThat(committedTransfer, is(expectedTransfer));
        verify(transfersDaoMock).addTransfer(eq(expectedTransfer));
        verify(transferChannelMock).add(eq(committedTransfer.getTransferId()));
    }

    @Test
    public void shouldFindTransferInDb() {
        //given
        Transfer transferInDb = transferBuilder.build();
        UUID transferId = transferInDb.getTransferId();
        when(transfersDaoMock.selectTransfer(transferId)).thenReturn(transferInDb);
        //then
        assertThat(transferService.find(transferId), is(transferInDb));
    }

    @Test
    public void shouldFindNoneIfTransferNotExists() {
        //given
        UUID unknownTransferId = UUID.randomUUID();
        //then
        assertThat(transferService.find(unknownTransferId), nullValue());
    }

    @Test
    @Parameters(method = "processingTransferCompletionStates")
    public void shouldCompleteOrRejectTransfer(Transfer.State state, Transfer.State completedState) {
        //given
        Transfer processingTransfer = transferBuilder.transferId(UUID.randomUUID()).state(state).build();
        Transfer expectedTransfer = processingTransfer.toBuilder().state(completedState).build();

        //when
        Transfer rejectingTransfer = transferService.complete(processingTransfer);
        //then
        assertThat(rejectingTransfer, is(expectedTransfer));
        verify(transfersDaoMock).updateTransfer(eq(expectedTransfer));
        verifyNoInteractions(transferChannelMock);
    }

    public Object[] processingTransferCompletionStates() {
        return new Object[][]{
                {PROCESSING, COMPLETED},
                {REJECTING, REJECTED}
        };
    }

    @Test
    public void shouldRejectingTransferOnReject() {
        //given
        Transfer processingTransfer = transferBuilder.transferId(UUID.randomUUID()).state(PROCESSING).build();
        Transfer expectedTransfer = processingTransfer.toBuilder().state(REJECTING).build();

        //when
        Transfer rejectingTransfer = transferService.reject(processingTransfer);
        //then
        assertThat(rejectingTransfer, is(expectedTransfer));
        verify(transfersDaoMock).updateTransfer(eq(expectedTransfer));
        verify(transferChannelMock).add(eq(rejectingTransfer.getTransferId()));
    }
}