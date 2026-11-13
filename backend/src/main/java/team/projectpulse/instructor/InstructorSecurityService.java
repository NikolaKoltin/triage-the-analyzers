package team.projectpulse.instructor;

import team.projectpulse.system.UserUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

/**
 * Answers the questions a route asks about one named instructor: is that instructor the caller themselves
 * ({@code isInstructorSelf}), and is the caller the admin of a course that instructor teaches
 * ({@code isCurrentUserAdminOfInstructorCourse}).
 *
 * <p>Every check fails closed: an id that names no row, or a link not yet established, is denied rather than
 * throwing. These methods run inside an {@code AuthorizationManager}, upstream of the {@code DispatcherServlet},
 * so an exception thrown here escapes the filter chain as a 500 that also tells the caller which ids exist.
 */
@Service
@Transactional
public class InstructorSecurityService {

    private final InstructorRepository instructorRepository;
    private final UserUtils userUtils;


    public InstructorSecurityService(InstructorRepository instructorRepository, UserUtils userUtils) {
        this.instructorRepository = instructorRepository;
        this.userUtils = userUtils;
    }

    public boolean isInstructorSelf(Integer instructorId) {
        Integer instructorIdFromJwt = this.userUtils.getUserId();
        return instructorId.equals(instructorIdFromJwt);
    }

    /**
     * Check if the user is the admin of the course that the instructor is in.
     *
     * @param instructorId the id of the instructor we need to access, this is NOT the current user's ID
     * @return
     */
    // PMD SimplifyBooleanReturns, not worth it: folding the null guard into "return instructor != null && ..." would
    // behave the same, but the explicit guard is how every *SecurityService in Project Pulse states the fail-closed
    // rule in this class comment (an id that names no row is denied); keeping one shape across all nine is the point.
    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    public boolean isCurrentUserAdminOfInstructorCourse(Integer instructorId) {
        Integer adminIdFromJwt = this.userUtils.getUserId();
        Instructor instructor = this.instructorRepository.findById(instructorId).orElse(null);
        if (instructor == null) {
            return false;
        }
        return instructor.getCourses().stream()
                .anyMatch(course -> course.getCourseAdmin().getId().equals(adminIdFromJwt));
    }

}
