package ui;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.application.TransactionGuard;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import refactoring.PolymorphismRefactoring;
import refactoring.ReplaceConditionalWithPolymorphism;
import refactoring.ReplaceTypeCodeWithStateStrategy;
import ui.abstractrefactorings.RefactoringType;
import ui.abstractrefactorings.RefactoringType.AbstractCandidateRefactoring;
import ui.abstractrefactorings.TypeCheckRefactoringType;
import ui.abstractrefactorings.TypeCheckRefactoringType.AbstractTypeCheckRefactoring;

import java.util.Collections;

/**
 * Panel for Type-State Checking refactorings.
 */
class TypeCheckingPanel extends AbstractRefactoringPanel {
    private static final String DETECT_INDICATOR_STATUS_TEXT_KEY = "type.state.checking.identification.indicator";
    private static final String[] COLUMN_NAMES = new String[]{
            "Type Checking Method",
            "Refactoring Type",
            "System-Level Occurrences",
            "Class-Level Occurrences",
            "Average #statements per case"
    };
    private static final int REFACTOR_DEPTH = 3;

    TypeCheckingPanel(@NotNull AnalysisScope scope) {
        super(  scope,
                DETECT_INDICATOR_STATUS_TEXT_KEY,
                new TypeCheckRefactoringType(scope.getProject()),
                new TypeCheckingTreeTableModel(
                        Collections.emptyList(),
                        COLUMN_NAMES,
                        scope.getProject()
                ),
                REFACTOR_DEPTH
        );
    }

    @Override
    protected void doRefactor(AbstractCandidateRefactoring candidateRefactoring) {
        AbstractTypeCheckRefactoring abstractRefactoring =
                (AbstractTypeCheckRefactoring) getAbstractRefactoringFromAbstractCandidateRefactoring(candidateRefactoring);
        PolymorphismRefactoring refactoring = abstractRefactoring.getRefactoring();

        Runnable applyRefactoring = () -> {
            removeHighlighters(scope.getProject());
            disableRefactoringsTable(true);
            WriteCommandAction.runWriteCommandAction(scope.getProject(), refactoring::apply);
        };

        if (refactoring instanceof ReplaceTypeCodeWithStateStrategy) {
            TransactionGuard.getInstance().submitTransactionAndWait(() -> {
                new ReplaceTypeCodeWithStateStrategyDialog(
                        (ReplaceTypeCodeWithStateStrategy) refactoring,
                        applyRefactoring
                ).show();
            });
        } else {
            applyRefactoring.run();
        }
    }
}