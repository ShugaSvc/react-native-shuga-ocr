
#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif

#import <CoreMedia/CoreMedia.h>

@interface RNShugaOcr : NSObject <RCTBridgeModule>

- (NSArray *)convertResultToRNMapFormat:(NSArray *)result;
- (void)scanTextInBuffer:(CMSampleBufferRef)buffer success:(void (^)(NSArray *successResult))success error:(void (^)(NSString * errorMessage))error;

@end
  
