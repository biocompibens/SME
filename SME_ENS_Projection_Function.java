package SME_PROJECTION_SRC;

/**
 * Created by rexhepaj on 16/03/16.
 * Abstract class that specifies structure of ray
 * function. Preprocessing should be done in derived class
 * constructors.
 */

abstract class SME_ENS_Projection_Function {

        /** Do actual slice projection for specific data types. */
        public abstract void projectSlice(byte[] pixels);
        public abstract void projectSlice(short[] pixels);
        public abstract void projectSlice(float[] pixels);

        /** Perform any necessary post processing operations, e.g.
         averging values. */
        public void postProcess() {}
    // end RayFunction
}
