package me.dio.credit.application.system.service.impl

import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.exception.BusinessException
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.service.ICreditService
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.time.LocalDate
import java.util.*

@Service
class CreditService(
    private val creditRepository: CreditRepository,
    private val customerService: CustomerService
) : ICreditService {

    override fun save(credit: Credit): Credit {
        validateNumberOfInstallments(credit.numberOfInstallments)
        validDayFirstInstallment(credit.dayFirstInstallment)
        credit.apply {
            customer = customerService.findById(credit.customer?.id!!)
        }

        return this.creditRepository.save(credit)
    }

    override fun findAllByCustomer(customerId: Long): List<Credit> =
        this.creditRepository.findAllByCustomerId(customerId)

    override fun findByCreditCode(customerId: Long, creditCode: UUID): Credit {
        val credit: Credit = (this.creditRepository.findByCreditCode(creditCode)
            ?: throw BusinessException("Creditcode $creditCode not found"))
        return if (credit.customer?.id == customerId) credit
        else throw IllegalArgumentException("Contact admin")
    }

    private fun validateNumberOfInstallments(numberOfInstallments: Int) {
        if (numberOfInstallments > 48) {
            throw BusinessException("Número máximo de parcelas excedido")
        }
    }

    private fun validDayFirstInstallment(dayFirstInstallment: LocalDate) {
        if (!dayFirstInstallment.isBefore(LocalDate.now().plusMonths(3))) {
            throw BusinessException("A data da primeira parcela deve ser no máximo 3 meses após a data atual.")
        }
    }
}