import model.Budget;
import service.BudgetService;
import service.ExpenseService;

public class Main {

    public static void main(String[] args) {

        int userId = 1;
        int month = 8;
        int year = 2026;

        BudgetService budgetService =
                new BudgetService();

        ExpenseService expenseService =
                new ExpenseService();

        Budget budget =
                budgetService.getBudget(
                        userId,
                        month,
                        year
                );

        double spent =
                expenseService.getMonthlySpent(
                        userId,
                        month,
                        year
                );

        if (budget != null) {

            double budgetAmount =
                    budget.getAmount();

            double remaining =
                    budgetAmount - spent;

            double percentage =
                    budgetService.getBudgetPercentage(
                            budgetAmount,
                            spent
                    );

            String status =
                    budgetService.getBudgetStatus(
                            budgetAmount,
                            spent
                    );

            System.out.println();
            System.out.println(
                    "========== BUDGET SUMMARY =========="
            );

            System.out.println(
                    "Budget: ₹" + budgetAmount
            );

            System.out.println(
                    "Spent: ₹" + spent
            );

            System.out.println(
                    "Remaining: ₹" + remaining
            );

            System.out.println(
                    "Used: " +
                    String.format("%.2f", percentage) +
                    "%"
            );

            System.out.println(
                    "Status: " + status
            );

        } else {

            System.out.println(
                    "No budget found."
            );
        }
    }
}