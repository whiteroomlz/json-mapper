package testingclasses;

import ru.hse.homework4.Exported;
import ru.hse.homework4.UnknownPropertiesPolicy;

@Exported(unknownPropertiesPolicy = UnknownPropertiesPolicy.FAIL)
public record Point3D(Double x, Double y, Double z) {
}
