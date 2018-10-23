
#import "RNShugaOcr.h"
//#import "FirebaseMLVision.h"
#import <FirebaseMLVision/FirebaseMLVision.h>

@implementation RNShugaOcr

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(scanTextInImage:(NSString *)data success:(RCTResponseSenderBlock)successCallback error:(RCTResponseSenderBlock)errorCallback) {
    NSData *bytes = [[NSData alloc]initWithBase64EncodedString:data options:NSDataBase64DecodingIgnoreUnknownCharacters];
    UIImage *image = [UIImage imageWithData:bytes];
    
    FIRVisionImage *firimage = [[FIRVisionImage alloc] initWithImage:image];
    
    [self scanTextInFirVisionImage:firimage success:^(NSArray *successResult) {
        //Convert our array of dictionary to the one for react native frontend
        NSArray *returnResult = [self convertResultToRNMapFormat:successResult];
        
        successCallback(@[returnResult]);
    } error:^(NSString *errorMessage) {
        if (errorMessage == nil) {
            errorCallback(@[[NSNull null]]);
        } else {
            errorCallback(@[errorMessage]);
        }
    }];
}

- (NSArray *)convertResultToRNMapFormat:(NSArray *)result {
    NSMutableArray *returnResult = [NSMutableArray new];
    for (NSDictionary *object in result) {
        NSString *text = object[@"description"];
        CGRect frame = [object[@"frame"] CGRectValue];
        NSArray *vertices = @[@{@"x": [NSNumber numberWithInteger:(NSInteger)floorf(frame.origin.x)],
                                @"y": [NSNumber numberWithInteger:(NSInteger)floorf(frame.origin.y)]},
                              @{@"x": [NSNumber numberWithInteger:(NSInteger)floorf(frame.origin.x + frame.size.width)],
                                @"y": [NSNumber numberWithInteger:(NSInteger)floorf(frame.origin.y)]},
                              @{@"x": [NSNumber numberWithInteger:(NSInteger)floorf(frame.origin.x)],
                                @"y": [NSNumber numberWithInteger:(NSInteger)floorf(frame.origin.y + frame.size.height)]},
                              @{@"x": [NSNumber numberWithInteger:(NSInteger)floorf(frame.origin.x + frame.size.width)],
                                @"y": [NSNumber numberWithInteger:(NSInteger)floorf(frame.origin.y + frame.size.height)]},
                              ];
        NSDictionary *obj = @{
                              @"description": text,
                              @"boundingPoly": @{@"vertices": vertices}
                              };
        [returnResult addObject:obj];
    }
    return [NSArray arrayWithArray:returnResult];
}

- (void)scanTextInFirVisionImage:(FIRVisionImage *)firimage success:(void (^)(NSArray *successResult))success error:(void (^)(NSString * errorMessage))error {
    FIRVision *vision = [FIRVision vision];
    FIRVisionTextRecognizer *textRecognizer = [vision onDeviceTextRecognizer];
    
    [textRecognizer processImage:firimage completion:^(FIRVisionText *_Nullable result, NSError *_Nullable firError) {
        if (firError != nil) {
            error(firError.debugDescription);
            return;
        }
        
        if (result == nil) {
            //Something is wrong with Firebase Vision configuration, maybe.
            error(nil); //must call or our rncamera text detector will keep on waiting forever
            return;
        }
        
        dispatch_async (dispatch_get_main_queue(), ^{
            NSMutableArray *mutableResult = [NSMutableArray new];
            for (FIRVisionTextBlock *block in result.blocks) {
                for (FIRVisionTextLine *line in block.lines) {
                    for (FIRVisionTextElement *element in line.elements) {
                        CGRect frame = element.frame;
                        [mutableResult addObject:@{
                                                   @"description": element.text,
                                                   @"frame": [NSValue valueWithCGRect:frame]
                                                   }];
                    }
                }
            }
            NSArray *result = [NSArray arrayWithArray:mutableResult];
            success(result);
        });
    }];
}

- (void)scanTextInBuffer:(CMSampleBufferRef)buffer success:(void (^)(NSArray *successResult))success error:(void (^)(NSString * errorMessage))error {
    FIRVisionImage *firimage = [[FIRVisionImage alloc] initWithBuffer:buffer];
    
    [self scanTextInFirVisionImage:firimage success:^(NSArray *successResult) {
        success(successResult);
    } error:^(NSString *errorMessage) {
        error(errorMessage);
    }];
}

@end
  
