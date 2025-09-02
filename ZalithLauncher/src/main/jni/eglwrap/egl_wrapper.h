#ifndef EGL_WRAPPER_H_
#define EGL_WRAPPER_H_

typedef void (*eglFuncPointer)(void); // getProcAddress function pointer type

extern eglFuncPointer (*sys_eglGetProcAddress)(const char* proc); // eglGetProcAddress function from the system EGL library

__attribute((constructor)) void init();
eglFuncPointer glXGetProcAddress(const char* proc);
eglFuncPointer eglGetProcAddress(const char* proc);

#endif