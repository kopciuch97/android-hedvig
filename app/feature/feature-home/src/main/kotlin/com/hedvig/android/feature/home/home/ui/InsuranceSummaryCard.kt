package com.hedvig.android.feature.home.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.home.home.data.InsuranceSummaryData
import com.hedvig.android.feature.home.home.data.PolicyInfo
import kotlinx.datetime.LocalDate

@Composable
internal fun InsuranceSummaryCard(
  data: InsuranceSummaryData,
  onViewDetailsClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val summaryDescription = buildString {
    append("${data.policies.size} active insurance policies")
    data.monthlyCost?.let { append(", monthly cost $it") }
    data.nextPaymentDate?.let { append(", next payment ${formatPaymentDate(it)}") }
  }
  HedvigCard(
    modifier = modifier.semantics { contentDescription = summaryDescription },
  ) {
    Column(Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        HedvigText(
          text = "Your Insurance",
          style = HedvigTheme.typography.headlineMedium,
          color = HedvigTheme.colorScheme.textPrimary,
        )
        HedvigText(
          text = "${data.policies.size} Active",
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }
      Spacer(Modifier.height(8.dp))
      HorizontalDivider()
      Spacer(Modifier.height(8.dp))
      data.policies.forEachIndexed { index, policy ->
        if (index > 0) {
          Spacer(Modifier.height(8.dp))
        }
        HedvigText(
          text = policy.displayName,
          style = HedvigTheme.typography.bodyMedium,
          color = HedvigTheme.colorScheme.textPrimary,
        )
        HedvigText(
          text = policy.exposureDisplayName,
          style = HedvigTheme.typography.bodySmall,
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }
      if (data.monthlyCost != null) {
        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          HedvigText(
            text = "${data.monthlyCost}/mo",
            style = HedvigTheme.typography.bodyMedium,
            color = HedvigTheme.colorScheme.textPrimary,
          )
          if (data.nextPaymentDate != null) {
            HedvigText(
              text = "Next payment: ${formatPaymentDate(data.nextPaymentDate)}",
              style = HedvigTheme.typography.bodySmall,
              color = HedvigTheme.colorScheme.textSecondary,
            )
          }
        }
      }
      Spacer(Modifier.height(8.dp))
      HorizontalDivider()
      Spacer(Modifier.height(8.dp))
      HedvigTextButton(
        text = "View details",
        onClick = onViewDetailsClick,
        buttonSize = Large,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

private fun formatPaymentDate(date: LocalDate): String {
  val day = date.day
  val month = date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
  return "$day $month"
}

// region previews
@HedvigPreview
@Composable
private fun PreviewInsuranceSummaryCardFull() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      InsuranceSummaryCard(
        data = InsuranceSummaryData(
          policies = listOf(
            PolicyInfo("Home Insurance", "Bellmansgatan 19A"),
            PolicyInfo("Car Insurance", "ABC 123"),
            PolicyInfo("Pet Insurance", "Fido"),
          ),
          monthlyCost = UiMoney(349.0, UiCurrencyCode.SEK),
          nextPaymentDate = LocalDate(2026, 5, 1),
        ),
        onViewDetailsClick = {},
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewInsuranceSummaryCardMinimal() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      InsuranceSummaryCard(
        data = InsuranceSummaryData(
          policies = listOf(
            PolicyInfo("Home Insurance", "Bellmansgatan 19A"),
          ),
          monthlyCost = null,
          nextPaymentDate = null,
        ),
        onViewDetailsClick = {},
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
      )
    }
  }
}
// endregion
