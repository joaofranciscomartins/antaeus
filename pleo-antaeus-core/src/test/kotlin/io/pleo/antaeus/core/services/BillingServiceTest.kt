package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.just
import java.math.BigDecimal
import org.junit.jupiter.api.Test

import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.Money
import io.pleo.antaeus.models.Currency

import io.pleo.antaeus.core.external.PaymentProvider


class BillingServiceTest {
    
    private val invoiceService = mockk<InvoiceService>()
    private val paymentProvider = mockk<PaymentProvider>()
    private val billingService = BillingService(paymentProvider, invoiceService)

    @Test
    fun `test monthlyPaymentsExecutionEnoughFunds`() {
        // Arrange
        val invoices: List<Invoice> = listOf(
            Invoice(1, 2, Money(BigDecimal.valueOf(20.00),Currency.EUR), InvoiceStatus.PENDING),
            Invoice(3, 4, Money(BigDecimal.valueOf(10.00),Currency.EUR), InvoiceStatus.PENDING)
        )

        every { invoiceService.fetchInvoicesByStatus(InvoiceStatus.PENDING) } returns invoices

        every { paymentProvider.charge(any()) } returns true

        every { invoiceService.updateInvoiceStatus(any(), any()) } returns Unit

        // Act
        billingService.monthlyPaymentsExecution()
        
        // Assert
        verify(exactly = invoices.size) { invoiceService.updateInvoiceStatus(any(), InvoiceStatus.PAID) }
    }

    @Test
    fun `test monthlyPaymentsExecutionNotEnoughFunds`() {
        // Arrange
        val invoices: List<Invoice> = listOf(
            Invoice(1, 2, Money(BigDecimal.valueOf(20.00),Currency.EUR), InvoiceStatus.PENDING),
            Invoice(3, 4, Money(BigDecimal.valueOf(10.00),Currency.EUR), InvoiceStatus.PENDING)
        )

        every { invoiceService.fetchInvoicesByStatus(InvoiceStatus.PENDING) } returns invoices

        every { paymentProvider.charge(any()) } returns false
        
        // Act
        billingService.monthlyPaymentsExecution()
        
        // Assert
        verify(exactly = 0) { invoiceService.updateInvoiceStatus(any(), InvoiceStatus.PAID) }
    }

    @Test
    fun `test monthlyPaymentsExecutionNoPendingInvoices`() {
        // Arrange
        val invoices: List<Invoice> = listOf()

        every { invoiceService.fetchInvoicesByStatus(InvoiceStatus.PENDING) } returns invoices

        // Act
        billingService.monthlyPaymentsExecution()
        
        // Assert
        verify(exactly = 0) { paymentProvider.charge(any()) }
    }
}

