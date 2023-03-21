package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.NetworkException


class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService

) {

    fun monthlyPaymentsExecution(){

        val invoices: List<Invoice> = invoiceService.fetchInvoicesByStatus(InvoiceStatus.PENDING)

        invoices.forEach{ invoice -> 
            try{
                if(paymentProvider.charge(invoice)){
                    // Since the invoice was sucessfully paid we update its value in the database
                    invoiceService.updateInvoiceStatus(invoice.id, InvoiceStatus.PAID)
                    println("Invoice with id: " + invoice.id + " was successfully paid.")
                }
                else{
                    // Let's keep the status of the invoice as PENDING since the customer
                    // didn't have enough funds to do the payment
                    println("Invoice with id: " + invoice.id + " was not paid due to lack of funds.")
                }
            }
            catch(e: CustomerNotFoundException) {
                println(e.message)
            }
            catch(e: CurrencyMismatchException) {
                println(e.message)
            }
            catch(e: NetworkException) {
                println(e.message)
            }
        }
    }
}
