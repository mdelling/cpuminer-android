/*
 * sched_bionic.h
 *
 *  Created on: Jan 26, 2014
 *      Author: matthewdellinger
 *      Source: https://github.com/android/platform_bionic/blob/master/libc/include/sched.h
 */

#ifndef SCHED_BIONIC_H_
#define SCHED_BIONIC_H_

#include <sched.h>
#include <sys/syscall.h>
#include <linux/sched.h>

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

/* Provide optimized implementation for 32-bit cpu_set_t */
#if CPU_SETSIZE == __CPU_BITS

# define CPU_ZERO(set_) \
		do{ \
			(set_)->__bits[0] = 0; \
		}while(0)

# define CPU_SET(cpu_,set_) \
		do {\
			size_t __cpu = (cpu_); \
			if (__cpu < CPU_SETSIZE) \
				(set_)->__bits[0] |= __CPU_MASK(__cpu); \
		}while (0)

# define CPU_CLR(cpu_,set_) \
		do {\
			size_t __cpu = (cpu_); \
			if (__cpu < CPU_SETSIZE) \
				(set_)->__bits[0] &= ~__CPU_MASK(__cpu); \
		}while (0)

# define CPU_ISSET(cpu_, set_) \
		(__extension__({\
			size_t __cpu = (cpu_); \
			(cpu_ < CPU_SETSIZE) \
				? ((set_)->__bits[0] & __CPU_MASK(__cpu)) != 0 \
				: 0; \
		}))

# define CPU_EQUAL(set1_, set2_) \
		((set1_)->__bits[0] == (set2_)->__bits[0])

# define __CPU_OP(dst_, set1_, set2_, op_) \
		do { \
			(dst_)->__bits[0] = (set1_)->__bits[0] op_ (set2_)->__bits[0]; \
		} while (0)

# define CPU_COUNT(set_) __builtin_popcountl((set_)->__bits[0])

#else /* CPU_SETSIZE != __CPU_BITS */

# define CPU_ZERO(set_) CPU_ZERO_S(sizeof(cpu_set_t), set_)
# define CPU_SET(cpu_,set_) CPU_SET_S(cpu_,sizeof(cpu_set_t),set_)
# define CPU_CLR(cpu_,set_) CPU_CLR_S(cpu_,sizeof(cpu_set_t),set_)
# define CPU_ISSET(cpu_,set_) CPU_ISSET_S(cpu_,sizeof(cpu_set_t),set_)
# define CPU_COUNT(set_) CPU_COUNT_S(sizeof(cpu_set_t),set_)
# define CPU_EQUAL(set1_,set2_) CPU_EQUAL_S(sizeof(cpu_set_t),set1_,set2_)

# define __CPU_OP(dst_,set1_,set2_,op_) __CPU_OP_S(sizeof(cpu_set_t),dst_,set1_,set2_,op_)

#endif /* CPU_SETSIZE != __CPU_BITS */

/* Wrapper for sched_setaffinity */
inline int sched_setaffinity(pid_t pid, size_t cpusetsize, cpu_set_t *mask) {
	return syscall(__NR_sched_setaffinity, pid, cpusetsize, mask);
}

#endif /* SCHED_BIONIC_H_ */
