package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.NetworkException


class BillingService(
    private val paymentProvider: PaymentProvider
) {

    fun monthlyPaymentsExecution(invoices: List<Invoice>): List<String> {

        val list: ArrayList<String> = ArrayList()

        invoices.forEach{ invoice -> 
            try{
                list.add("Invoice with id: " + invoice.id + " was successfully payed: " + paymentProvider.charge(invoice))
            }
            catch(e: CustomerNotFoundException) {
                println("Payment failed because there is no customer with a matching id: ${e.message}")
            }
            catch(e: CurrencyMismatchException) {
                println("Payment failed because the currency in the invoice does not match the customers: ${e.message}")
            }
            catch(e: NetworkException) {
                println("Payment failed because of a network error: ${e.message}")
            }
        }
        return list
    }
}
