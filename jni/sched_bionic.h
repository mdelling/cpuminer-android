/*
 * sched_bionic.h
 *
 *  Created on: Jan 26, 2014
 *      Author: matthewdellinger
 */

#ifndef SCHED_BIONIC_H_
#define SCHED_BIONIC_H_

#include <sched.h>

/* Our implementation supports up to 32 independent CPUs, which is also
* the maximum supported by the kernel at the moment. GLibc uses 1024 by
* default.
*
* If you want to use more than that, you should use CPU_ALLOC() / CPU_FREE()
* and the CPU_XXX_S() macro variants.
*/
#define CPU_SETSIZE 32

#define __CPU_BITTYPE unsigned long int /* mandated by the kernel */
#define __CPU_BITSHIFT 5 /* should be log2(BITTYPE) */
#define __CPU_BITS (1 << __CPU_BITSHIFT)
#define __CPU_ELT(x) ((x) >> __CPU_BITSHIFT)
#define __CPU_MASK(x) ((__CPU_BITTYPE)1 << ((x) & (__CPU_BITS-1)))

typedef struct {
    __CPU_BITTYPE __bits[ CPU_SETSIZE / __CPU_BITS ];
} cpu_set_t;

#endif /* SCHED_BIONIC_H_ */
