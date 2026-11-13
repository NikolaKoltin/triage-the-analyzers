package team.projectpulse.instructor;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InstructorSpecsTest {

    private Locale originalLocale;

    @BeforeEach
    void rememberLocale() {
        this.originalLocale = Locale.getDefault();
    }

    @AfterEach
    void restoreLocale() {
        Locale.setDefault(this.originalLocale);
    }

    @Test
    @DisplayName("UseLocaleWithCaseConversions: a first-name search lowercases the way MySQL's lower() does, even on a JVM whose default locale is Turkish")
    @SuppressWarnings("unchecked")
    void hasFirstNameLowercasesIndependentlyOfDefaultLocale() {
        // In Turkish, "I".toLowerCase() is the dotless "ı", so with the default locale "IVAN" becomes "ıvan",
        // which never equals the database's lower(first_name) = "ivan": the search silently finds no one.
        Locale.setDefault(Locale.forLanguageTag("tr-TR"));
        Root<Instructor> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Path<String> firstName = mock(Path.class);
        Expression<String> loweredFirstName = mock(Expression.class);
        when(root.<String>get("firstName")).thenReturn(firstName);
        when(criteriaBuilder.lower(firstName)).thenReturn(loweredFirstName);

        InstructorSpecs.hasFirstName("IVAN").toPredicate(root, query, criteriaBuilder);

        verify(criteriaBuilder).equal(loweredFirstName, "ivan");
    }

}
