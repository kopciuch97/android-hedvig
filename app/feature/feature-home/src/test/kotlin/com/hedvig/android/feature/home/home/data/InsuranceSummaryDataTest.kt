package com.hedvig.android.feature.home.home.data

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import kotlinx.datetime.LocalDate
import org.junit.Test

internal class InsuranceSummaryDataTest {
  @Test
  fun `construct InsuranceSummaryData with multiple policies and verify field access`() {
    val policies = listOf(
      PolicyInfo(
        displayName = "Home Insurance",
        exposureDisplayName = "Kungsgatan 1",
      ),
      PolicyInfo(
        displayName = "Car Insurance",
        exposureDisplayName = "ABC 123",
      ),
    )
    val monthlyCost = UiMoney(299.0, UiCurrencyCode.SEK)
    val nextPaymentDate = LocalDate(2026, 5, 1)

    val summary = InsuranceSummaryData(
      policies = policies,
      monthlyCost = monthlyCost,
      nextPaymentDate = nextPaymentDate,
    )

    assertThat(summary).prop(InsuranceSummaryData::policies).hasSize(2)
    assertThat(summary).prop(InsuranceSummaryData::policies).transform { it[0] }
      .prop(PolicyInfo::displayName).isEqualTo("Home Insurance")
    assertThat(summary).prop(InsuranceSummaryData::policies).transform { it[0] }
      .prop(PolicyInfo::exposureDisplayName).isEqualTo("Kungsgatan 1")
    assertThat(summary).prop(InsuranceSummaryData::policies).transform { it[1] }
      .prop(PolicyInfo::displayName).isEqualTo("Car Insurance")
    assertThat(summary).prop(InsuranceSummaryData::monthlyCost).isNotNull()
      .isEqualTo(UiMoney(299.0, UiCurrencyCode.SEK))
    assertThat(summary).prop(InsuranceSummaryData::nextPaymentDate).isNotNull()
      .isEqualTo(LocalDate(2026, 5, 1))
  }

  @Test
  fun `InsuranceSummaryData with null monthlyCost and null nextPaymentDate is valid`() {
    val summary = InsuranceSummaryData(
      policies = listOf(
        PolicyInfo(
          displayName = "Home Insurance",
          exposureDisplayName = "Kungsgatan 1",
        ),
      ),
      monthlyCost = null,
      nextPaymentDate = null,
    )

    assertThat(summary).prop(InsuranceSummaryData::monthlyCost).isNull()
    assertThat(summary).prop(InsuranceSummaryData::nextPaymentDate).isNull()
    assertThat(summary).prop(InsuranceSummaryData::policies).hasSize(1)
  }

  @Test
  fun `InsuranceSummaryData with empty policies list is constructable`() {
    val summary = InsuranceSummaryData(
      policies = emptyList(),
      monthlyCost = UiMoney(0.0, UiCurrencyCode.SEK),
      nextPaymentDate = null,
    )

    assertThat(summary).prop(InsuranceSummaryData::policies).isEmpty()
  }
}
