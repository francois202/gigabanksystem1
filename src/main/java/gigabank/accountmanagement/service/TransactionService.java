package gigabank.accountmanagement.service;

import gigabank.accountmanagement.dto.response.TransactionResponse;
import gigabank.accountmanagement.enums.TransactionType;
import gigabank.accountmanagement.mapper.TransactionMapper;
import gigabank.accountmanagement.model.TransactionEntity;
import gigabank.accountmanagement.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис отвечает за управление платежами и переводами
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    /**
     * Получает все транзакции для указанного счета.
     *
     * @param accountId идентификатор счета
     * @return список DTO транзакций для указанного счета
     */
    public List<TransactionResponse> getAccountTransactions(Long accountId) {
        log.info("Получение транзакций для счета ID: {}", accountId);

        List<TransactionEntity> transactionEntities = transactionRepository.findByAccountId(accountId);

        List<TransactionResponse> response = transactionEntities.stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());

        log.info("Найдено {} транзакций для счета ID {}", response.size(), accountId);
        return response;
    }

    /**
     * Получает отфильтрованный список транзакций с поддержкой пагинации.
     *
     * @param accountId идентификатор счета для фильтрации (опционально)
     * @param type тип транзакции для фильтрации (опционально)
     * @param category категория транзакции для фильтрации (опционально)
     * @param startDate начальная дата для фильтрации по периоду (опционально)
     * @param pageable параметры пагинации и сортировки
     * @return страница с отфильтрованными транзакциями в формате DTO
     */
    public Page<TransactionResponse> getFilteredTransactions(Long accountId, TransactionType type,
                                                           String category, LocalDateTime startDate,
                                                           Pageable pageable) {
        log.info("Попытка поиска транзакций по фильтру.");
        String accountIdStr = accountId != null ? accountId.toString() : null;
        String typeStr = type != null ? type.name() : null;
        String startDateStr = startDate != null ? startDate.toString() : null;

        Page<TransactionEntity> transactionEntities = transactionRepository.findWithFiltersNative(
                accountIdStr, typeStr, category, startDateStr, pageable);

        log.info("Найдено {} транзакций", transactionEntities.getTotalElements());
        return transactionEntities.map(transactionMapper::toResponse);
    }
}
