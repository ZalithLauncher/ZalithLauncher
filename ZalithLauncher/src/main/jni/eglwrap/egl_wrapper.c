/* Simple system EGL/OpenGL driver stub */
/* created by whitebelyash on 26.08.25 */

#include "egl_wrapper.h"

#include <EGL/egl.h>
#include <stdlib.h>
#include <stdio.h>
#include <dlfcn.h>

eglFuncPointer (*sys_eglGetProcAddress)(const char* proc);

__attribute((constructor)) void init(){
    // Open system EGL
    void* handle = dlopen("libEGL.so", RTLD_LAZY | RTLD_LOCAL);
    if(!handle){
        fprintf(stderr, "EGLWrapper: Failed to load system EGL library!\n");
        abort();
    }
    // Load system proc loader from the EGL lib
    sys_eglGetProcAddress = dlsym(handle, "eglGetProcAddress");
    if(!sys_eglGetProcAddress){
        fprintf(stderr, "EGLWrapper: Unable to locate eglGetProcAddress symbol in EGL!\n");
        abort();
    }
}

// Stub required for LWJGL to locate GL/GLES functions
eglFuncPointer glXGetProcAddress(const char* proc){
    eglFuncPointer func = (eglFuncPointer) sys_eglGetProcAddress(proc);
    if(!func){
        fprintf(stderr, "EGLWrapper: Unknown function %s!\n", proc);
        return NULL;
    }
    return func;
}
// Stub required for libpojavexec - it loads EGL functions via wrapper library
eglFuncPointer eglGetProcAddress(const char* proc){
    return sys_eglGetProcAddress(proc);
}

