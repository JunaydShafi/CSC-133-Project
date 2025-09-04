package pkgDriver;


import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryUtil.*;


public class Driver {
    GLFWErrorCallback errorCallback;
    GLFWKeyCallback keyCallback;
    GLFWFramebufferSizeCallback fbCallback;
    long window;

    // window
    private static  int WIN_WIDTH = 1000;// replacing the random number struct for vertecies and indexs
    private static  int WIN_HEIGHT = 800;
    private static final int WIN_POS_X = 30;
    private static final int WIN_POX_Y = 90;

    private static final int MSAA_SAMPLES = 8; // fixing magic numbers
    private static final int VSYNC_INTERVAL = 1;

    private static final float ORTHO_LEFT = -100f; // all used for viewProjMatrix.setOrtho();
    private static final float ORTHO_RIGHT = 100f;
    private static final float ORTHO_BOTTOM = -100f;
    private static final float ORTHO_TOP = 100f;
    private static final float ORTHO_NEAR = 0f;
    private static final float ORTHO_FAR = 10f;

    // color (green)
    private static final float[] SQUARE_COLOR = {0.5f, 1.0f, 0.0f};
    // background
    private static final float[] CLEAR_COLOR = {0.043f, 0.380f, 0.588f, 1.0f};

    private static final int OGL_MATRIX_SIZE = 16;
    // call glCreateProgram() here - we have no gl-context here

    int shader_program;
    Matrix4f viewProjMatrix = new Matrix4f();
    FloatBuffer myFloatBuffer = BufferUtils.createFloatBuffer(OGL_MATRIX_SIZE);
    int vpMatLocation = 0, renderColorLocation = 0;

    public static void main(String[] myArgs) {
        new Driver().render();
    } // public static void main(String[] args)

    void render() {
        try {
            initGLFWindow();
            renderLoop();
            glfwDestroyWindow(window);
            keyCallback.free();
            fbCallback.free();
        } finally {
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    } // void render()

    private void initGLFWindow() {
        glfwSetErrorCallback(errorCallback =
                GLFWErrorCallback.createPrint(System.err));
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, MSAA_SAMPLES);
        window = glfwCreateWindow(WIN_WIDTH, WIN_HEIGHT, "CSC 133", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int
                    mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                    glfwSetWindowShouldClose(window, true);
            }
        });

        glfwSetFramebufferSizeCallback(window, fbCallback = new
                GLFWFramebufferSizeCallback() {
                    @Override
                    public void invoke(long window, int w, int h) {
                        if (w > 0 && h > 0) {
                            WIN_WIDTH = w;
                            WIN_HEIGHT = h;
                        }
                    }
                });

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, WIN_POS_X, WIN_POX_Y);
        glfwMakeContextCurrent(window);
        glfwSwapInterval(VSYNC_INTERVAL);
        glfwShowWindow(window);
    } // private void initGLFWindow()

    void renderLoop() {
        initOpenGL();
        renderObjects();
    } // void renderLoop()

    void initOpenGL() {
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glViewport(0, 0, WIN_WIDTH, WIN_HEIGHT);
        glClearColor(CLEAR_COLOR[0], CLEAR_COLOR[1], CLEAR_COLOR[2], CLEAR_COLOR[3]);

        // shader using built-ins
        shader_program = glCreateProgram();

        int vs = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs,
                "uniform mat4 viewProjMatrix;" +
                        "void main(void) {" +
                        "  gl_Position = viewProjMatrix * gl_Vertex;" +
                        "}");
        glCompileShader(vs);
        glAttachShader(shader_program, vs);

        int fs = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs,
                "uniform vec3 color;" +
                        "void main(void) {" +
                        "  gl_FragColor = vec4(0.5f, 1.0f, 0.0f, 1.0f);" +
                        "}");
        glCompileShader(fs);
        glAttachShader(shader_program, fs);

        glLinkProgram(shader_program);
        glUseProgram(shader_program);

        vpMatLocation = glGetUniformLocation(shader_program, "viewProjMatrix");
        renderColorLocation = glGetUniformLocation(shader_program, "color");

        // set projection once
        viewProjMatrix.setOrtho(ORTHO_LEFT, ORTHO_RIGHT, ORTHO_BOTTOM, ORTHO_TOP, ORTHO_NEAR, ORTHO_FAR);
        glUniformMatrix4fv(vpMatLocation, false, viewProjMatrix.get(myFloatBuffer));
    }


    // Create a square *centered* at the origin (0,0)
    private float[] createCenteredSquare(float side) {
        float half = side / 2f;
        return new float[]{
                -half, -half,   // bottom-left
                half, -half,   // bottom-right
                half,  half,   // top-right
                -half,  half    // top-left
        };
    }

    void renderObjects() {
        int vbo = glGenBuffers();
        int ibo = glGenBuffers();// both variables are buffers

        final float RECT_WIDTH = 40f;   // full width of rectangle
        final float RECT_HEIGHT = 40f;     // full height of rectangle
        final int NUM_TRIANGLES = 2;       // 2 triangles form rectangle
        final int VERTICES_PER_TRIANGLE = 3;
        final int NUM_INDICES = NUM_TRIANGLES * VERTICES_PER_TRIANGLE; // 6

        float halfWidth = RECT_WIDTH / 2;
        float halfHeight = RECT_HEIGHT / 2;

        float[] vertices = {
                -halfWidth, -halfHeight,
                halfWidth, -halfHeight,
                halfWidth,  halfHeight,
                -halfWidth,  halfHeight
        };

        int[] indices = {0, 1, 2, 0, 2, 3};

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, (FloatBuffer) BufferUtils.
                createFloatBuffer(vertices.length).
                put(vertices).flip(), GL_STATIC_DRAW);
        glEnableClientState(GL_VERTEX_ARRAY);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, (IntBuffer) BufferUtils.
                createIntBuffer(indices.length).
                put(indices).flip(), GL_STATIC_DRAW);
        final int VERTEX_COMPONENTS = 2;
        glVertexPointer(VERTEX_COMPONENTS, GL_FLOAT, 0, 0L);
        viewProjMatrix.setOrtho(ORTHO_LEFT, ORTHO_RIGHT , ORTHO_BOTTOM , ORTHO_TOP , ORTHO_NEAR , ORTHO_FAR);
        glUniformMatrix4fv(vpMatLocation, false,
                viewProjMatrix.get(myFloatBuffer));
        glUniform3f(renderColorLocation, 1.0f, 0.498f, 0.153f);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        int VTD = 6; // need to process 6 Vertices To Draw 2 triangles
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glDrawElements(GL_TRIANGLES, NUM_INDICES, GL_UNSIGNED_INT, 0L);
            glfwSwapBuffers(window);
        }
    } // renderObjects
}


